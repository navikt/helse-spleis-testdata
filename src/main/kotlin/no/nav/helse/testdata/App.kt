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
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.speed.SpeedClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import no.nav.helse.rapids_rivers.*
import no.nav.helse.testdata.api.*
import no.nav.helse.testdata.rivers.PersonSlettetRiver
import no.nav.helse.testdata.rivers.VedtaksperiodeEndretRiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

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
        install(ClientContentNegotiation) {
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
        rapidsConfig = RapidApplication.RapidApplicationConfig.fromEnv(System.getenv()),
        subscriptionService = ConcreteSubscriptionService,
        inntektRestClient = inntektRestClient,
        aaregClient = aaregClient,
        eregClient = eregClient,
        speedClient = speedClient,
        azureAd = azureAd
    ).start()
}

internal class ApplicationBuilder(
    rapidsConfig: RapidApplication.RapidApplicationConfig,
    private val subscriptionService: SubscriptionService,
    private val inntektRestClient: InntektRestClient,
    private val aaregClient: AaregClient,
    private val eregClient: EregClient,
    private val speedClient: SpeedClient,
    azureAd: RefreshTokens
) : RapidsConnection.StatusListener {
    private lateinit var rapidsMediator: RapidsMediator

    private val rapidsConnection =
        RapidApplication.Builder(rapidsConfig)
            .withKtorModule {
                installKtorModule(
                    subscriptionService,
                    inntektRestClient,
                    aaregClient,
                    eregClient,
                    speedClient,
                    rapidsMediator
                )
            }.build()

    init {
        rapidsMediator = RapidsMediator(rapidsConnection)
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
    installJacksonFeature()
    install(WebSockets)
    install(CallLogging) {
        logger = LoggerFactory.getLogger("no.nav.helse.testdata.CallLogging")
        level = Level.INFO
        disableDefaultColors()
        filter { call -> setOf("/metrics", "/isalive", "/isready").none { call.request.path().contains(it) } }
    }
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

internal fun Application.installJacksonFeature() {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
}

private class TokenRefreshRiver(rapidsConnection: RapidsConnection, private val azureAd: RefreshTokens) : River.PacketListener {
    init {
        River(rapidsConnection)
            .validate { it.demandValue("@event_name", "halv_time") }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
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
