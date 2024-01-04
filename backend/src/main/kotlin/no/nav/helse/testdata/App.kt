package no.nav.helse.testdata

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.testdata.api.*
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

    val azureAd = AzureAd(AzureAdProperties(env))
    val inntektRestClient = InntektRestClient(env.inntektRestUrl, env.inntektResourceId, azureAd::accessToken, httpClient)
    val aaregClient = AaregClient(env.aaregUrl, env.aaregScope, azureAd::accessToken, httpClient)

    ApplicationBuilder(
        rapidsConfig = RapidApplication.RapidApplicationConfig.fromEnv(System.getenv()),
        subscriptionService = ConcreteSubscriptionService,
        inntektRestClient = inntektRestClient,
        aaregClient = aaregClient
    ).start()
}

internal class ApplicationBuilder(
    rapidsConfig: RapidApplication.RapidApplicationConfig,
    private val subscriptionService: SubscriptionService,
    private val inntektRestClient: InntektRestClient,
    private val aaregClient: AaregClient
) : RapidsConnection.StatusListener {
    private lateinit var rapidsMediator: RapidsMediator

    private val rapidsConnection =
        RapidApplication.Builder(rapidsConfig)
            .withKtorModule {
                installKtorModule(
                    subscriptionService,
                    inntektRestClient,
                    aaregClient,
                    rapidsMediator
                )
            }.build()

    init {
        rapidsMediator = RapidsMediator(rapidsConnection)
        rapidsConnection.register(this)
        VedtaksperiodeEndretRiver(rapidsConnection, subscriptionService)
    }

    fun start() = rapidsConnection.start()
}

internal fun Application.installKtorModule(
    subscriptionService: SubscriptionService,
    inntektRestClient: InntektRestClient,
    aaregClient: AaregClient,
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
        registerPersonApi(rapidsMediator)
        registerVedtaksperiodeApi(rapidsMediator)
        registerArbeidsforholdApi(aaregClient)
        registerInntektApi(inntektRestClient)
        registerBehovApi(rapidsMediator)
        registerSubscriptionApi(subscriptionService)

        static("/") {
            staticRootFolder = File("public")
            files("")
            default("index.html")
        }
    }
}

private fun Application.errorTracing(logger: Logger) {
    intercept(ApplicationCallPipeline.Monitoring) {
        try {
            proceed()
        } catch (err: Throwable) {
            logger.error("exception thrown during processing: ${err.message} callId=${call.callId}", err)
        }
    }
}

internal fun Application.installJacksonFeature() {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
}
