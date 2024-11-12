package no.nav.helse.testdata

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.azure.createJwkAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.kafka.AivenConfig
import com.github.navikt.tbd_libs.kafka.ConsumerProducerFactory
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.speed.SpeedClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.rapids_rivers.*
import no.nav.helse.testdata.api.*
import no.nav.helse.testdata.rivers.PersonSlettetRiver
import no.nav.helse.testdata.rivers.VedtaksperiodeEndretRiver
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val sikkerlogg: Logger = LoggerFactory.getLogger("tjenestekall")
val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .setDefaultPrettyPrinter(
        DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        }
    )

fun main() {
    val env = setUpEnvironment()

    val httpClient = HttpClient(CIO) {
        expectSuccess = false
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }
    }

    val azureAd = RefreshTokens(createJwkAzureTokenClientFromEnvironment())
    val inntektRestClient = InntektRestClient(env.inntektRestUrl, env.inntektScope, azureAd, httpClient)
    val aaregClient = AaregClient(env.aaregUrl, env.aaregScope, azureAd, httpClient)
    val eregClient = EregClient(env.eregUrl, httpClient)
    val speedClient = SpeedClient(
        httpClient = java.net.http.HttpClient.newHttpClient(),
        objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
        tokenProvider = azureAd
    )

    ApplicationBuilder(
        env = System.getenv(),
        subscriptionService = ConcreteSubscriptionService,
        inntektRestClient = inntektRestClient,
        aaregClient = aaregClient,
        eregClient = eregClient,
        speedClient = speedClient,
        azureAd = azureAd
    ).start()
}

internal class ApplicationBuilder(
    env: Map<String, String>,
    private val subscriptionService: SubscriptionService,
    private val inntektRestClient: InntektRestClient,
    private val aaregClient: AaregClient,
    private val eregClient: EregClient,
    private val speedClient: SpeedClient,
    azureAd: RefreshTokens
) : RapidsConnection.StatusListener {
    private val factory = ConsumerProducerFactory(AivenConfig.default)
    private val rapidsMediator = RapidsMediator(object : RapidProducer {
        private val producer = factory.createProducer()
        override fun publish(message: String) {
            producer.send(ProducerRecord(env.getValue("KAFKA_RAPID_TOPIC"), message))
        }

        override fun publish(key: String, message: String) {
            producer.send(ProducerRecord(env.getValue("KAFKA_RAPID_TOPIC"), key, message))
        }
    })

    private val rapidsConnection =
        RapidApplication.create(
            env = env,
            consumerProducerFactory = factory,
            builder = {
                withKtorModule {
                    installKtorModule(
                        subscriptionService,
                        inntektRestClient,
                        aaregClient,
                        eregClient,
                        speedClient,
                        rapidsMediator
                    )
                }
            }
        )

    init {
        rapidsConnection.register(this)
        VedtaksperiodeEndretRiver(rapidsConnection, subscriptionService)
        PersonSlettetRiver(rapidsConnection, subscriptionService)
        TokenRefreshRiver(rapidsConnection, azureAd)
    }

    fun start() = rapidsConnection.start()
}

internal fun Application.installKtorModule(
    subscriptionService: SubscriptionService,
    inntektRestClient: InntektRestClient,
    aaregClient: AaregClient,
    eregClient: EregClient,
    speedClient: SpeedClient,
    rapidsMediator: RapidsMediator,
) {
    install(WebSockets)
    errorTracing(no.nav.helse.testdata.log)

    routing {
        registerPersonApi(rapidsMediator, speedClient)
        registerVedtaksperiodeApi(rapidsMediator)
        registerArbeidsforholdApi(aaregClient)
        registerOrganisasjonApi(eregClient)
        registerInntektApi(inntektRestClient)
        registerBehovApi(rapidsMediator)
        registerSubscriptionApi(subscriptionService)

        staticFiles("/", File("public"))
    }
}

private fun Application.errorTracing(logger: Logger) {
    intercept(ApplicationCallPipeline.Monitoring) {
        try {
            proceed()
        } catch (err: Throwable) {
            logger.error("En feil oppsto: ${err.message} callId=${call.callId}", err)
        }
    }
}

private class TokenRefreshRiver(rapidsConnection: RapidsConnection, private val azureAd: RefreshTokens) : River.PacketListener {
    init {
        River(rapidsConnection)
            .validate { it.demandValue("@event_name", "halv_time") }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        log.info("refresher tokens som har gått ut")
        azureAd.refreshTokens()
    }
}

class RefreshTokens(private val client: AzureTokenProvider) : AzureTokenProvider by (client) {
    private val scopes = mutableSetOf<String>()
    fun refreshTokens() {
        scopes.forEach { scope ->
            log.info("refresher $scope")
            try {
                client.bearerToken(scope)
            } catch (err: Exception) {
                log.info("refresh gikk ikke så bra: ${err.message}", err)
            }
        }
    }
    override fun bearerToken(scope: String): Result<AzureToken> {
        scopes.add(scope)
        return client.bearerToken(scope)
    }
}
