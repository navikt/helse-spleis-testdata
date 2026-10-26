package no.nav.helse.testdata

import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.azure.createJwkAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.kafka.AivenConfig
import com.github.navikt.tbd_libs.kafka.ConsumerProducerFactory
import com.github.navikt.tbd_libs.naisful.naisApp
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.speed.SpeedClient
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.jackson3.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.header
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.helse.rapids_rivers.*
import no.nav.helse.testdata.api.*
import no.nav.helse.testdata.db.ForsikringReplikaTestdataDao
import no.nav.helse.testdata.db.ForsikringReplikaTestdataDataSource
import no.nav.helse.testdata.rivers.PersonSlettetRiver
import no.nav.helse.testdata.rivers.TrengerOpplysningerFraArbeidsgiverRiver
import no.nav.helse.testdata.rivers.VedtaksperiodeEndretRiver
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tools.jackson.core.util.DefaultIndenter
import tools.jackson.core.util.DefaultPrettyPrinter
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.nio.file.Paths

val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val sikkerlogg: Logger = LoggerFactory.getLogger("tjenestekall")
val objectMapper: ObjectMapper =
    jacksonMapperBuilder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        // Jackson 3 skrur dette på som standard; frontenden kan utelate tallfelter som sykmeldingsgrad
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        // Standardvalideringen godtar ikke felter som starter med æ, ø eller å (f.eks. «år»), og dropper dem stille
        .accessorNaming(DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true))
        .defaultPrettyPrinter(
            DefaultPrettyPrinter()
                .withArrayIndenter(DefaultPrettyPrinter.FixedSpaceIndenter.instance())
                .withObjectIndenter(DefaultIndenter("  ", "\n")),
        ).build()

fun main() {
    val env = setUpEnvironment()

    val httpClient =
        HttpClient(CIO) {
            expectSuccess = false
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(objectMapper))
            }
        }

    val azureAd = RefreshTokens(createJwkAzureTokenClientFromEnvironment())
    val inntektRestClient = InntektRestClient(env.inntektRestUrl, env.inntektScope, azureAd, httpClient)
    val aaregClient = AaregClient(env.aaregUrl, env.aaregScope, azureAd, httpClient)
    val eregClient = EregClient(env.eregUrl, httpClient)
    val speedClient =
        SpeedClient(
            httpClient =
                java.net.http.HttpClient
                    .newHttpClient(),
            objectMapper = jacksonObjectMapper(),
            tokenProvider = azureAd,
        )

    val forsikringReplikaTestdataDataSource = ForsikringReplikaTestdataDataSource.createDataSource(env)
    ForsikringReplikaTestdataDataSource.migrate(forsikringReplikaTestdataDataSource)
    val forsikringReplikaTestdataDao = ForsikringReplikaTestdataDao(forsikringReplikaTestdataDataSource)

    ApplicationBuilder(
        env = System.getenv(),
        subscriptionService = ConcreteSubscriptionService,
        inntektRestClient = inntektRestClient,
        aaregClient = aaregClient,
        eregClient = eregClient,
        speedClient = speedClient,
        azureAd = azureAd,
        forsikringReplikaTestdataDataSource = forsikringReplikaTestdataDataSource,
        forsikringReplikaTestdataDao = forsikringReplikaTestdataDao,
    ).start()
}

internal class ApplicationBuilder(
    env: Map<String, String>,
    private val subscriptionService: SubscriptionService,
    private val inntektRestClient: InntektRestClient,
    private val aaregClient: AaregClient,
    private val eregClient: EregClient,
    private val speedClient: SpeedClient,
    azureAd: RefreshTokens,
    private val forsikringReplikaTestdataDataSource: HikariDataSource,
    private val forsikringReplikaTestdataDao: ForsikringReplikaTestdataDao,
) : RapidsConnection.StatusListener {
    private val factory = ConsumerProducerFactory(AivenConfig.default)
    private val rapidsMediator =
        RapidsMediator(
            object : RapidProducer {
                private val producer = factory.createProducer()

                override fun publish(message: String) {
                    producer.send(ProducerRecord(env.getValue("KAFKA_RAPID_TOPIC"), message))
                }

                override fun publish(
                    key: String,
                    message: String,
                ) {
                    producer.send(ProducerRecord(env.getValue("KAFKA_RAPID_TOPIC"), key, message))
                }
            },
        )

    private val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, PrometheusRegistry.defaultRegistry, Clock.SYSTEM)
    private val rapidsConnection =
        RapidApplication.create(
            env = env,
            meterRegistry = meterRegistry,
            consumerProducerFactory = factory,
            builder = {
                withKtor { preStopHook, rapid ->
                    naisApp(
                        meterRegistry = meterRegistry,
                        objectMapper = objectMapper,
                        applicationLogger = LoggerFactory.getLogger("no.nav.helse.testdata.App"),
                        callLogger = LoggerFactory.getLogger("no.nav.helse.testdata.CallLogging"),
                        naisEndpoints = com.github.navikt.tbd_libs.naisful.NaisEndpoints.Default,
                        timersConfig = { call, _ ->
                            this
                                // https://github.com/linkerd/polixy/blob/main/DESIGN.md#l5d-client-id-client-id
                                // eksempel: <APP>.<NAMESPACE>.serviceaccount.identity.linkerd.cluster.local
                                .tag("konsument", call.request.header("L5d-Client-Id") ?: "n/a")
                        },
                        mdcEntries =
                            mapOf(
                                "konsument" to { call: ApplicationCall -> call.request.header("L5d-Client-Id") },
                            ),
                        aliveCheck = rapid::isReady,
                        readyCheck = rapid::isReady,
                        preStopHook = preStopHook::handlePreStopRequest,
                    ) {
                        installKtorModule(
                            subscriptionService,
                            inntektRestClient,
                            aaregClient,
                            eregClient,
                            speedClient,
                            rapidsMediator,
                            forsikringReplikaTestdataDao,
                        )
                    }
                }
            },
        )

    init {
        rapidsConnection.register(this)
        VedtaksperiodeEndretRiver(rapidsConnection, subscriptionService)
        PersonSlettetRiver(rapidsConnection, subscriptionService)
        TrengerOpplysningerFraArbeidsgiverRiver(rapidsConnection, subscriptionService)
        TokenRefreshRiver(rapidsConnection, azureAd)
    }

    fun start() = rapidsConnection.start()

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        forsikringReplikaTestdataDataSource.close()
    }
}

internal fun Application.installKtorModule(
    subscriptionService: SubscriptionService,
    inntektRestClient: InntektRestClient,
    aaregClient: AaregClient,
    eregClient: EregClient,
    speedClient: SpeedClient,
    rapidsMediator: RapidsMediator,
    forsikringReplikaTestdataDao: ForsikringReplikaTestdataDao,
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
        registerForsikringReplikaApi(forsikringReplikaTestdataDao)

        val path = Paths.get("").toAbsolutePath().toString()
        val dir = File("public")
        check(dir.exists() && dir.isDirectory) {
            "$dir (cwd: $path) is not a directory"
        }
        staticFiles("/", dir)
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

private class TokenRefreshRiver(
    rapidsConnection: RapidsConnection,
    private val azureAd: RefreshTokens,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .precondition { it.requireValue("@event_name", "halv_time") }
            .register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        log.info("refresher tokens som har gått ut")
        azureAd.refreshTokens()
    }
}

class RefreshTokens(
    private val client: AzureTokenProvider,
) : AzureTokenProvider by (client) {
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
