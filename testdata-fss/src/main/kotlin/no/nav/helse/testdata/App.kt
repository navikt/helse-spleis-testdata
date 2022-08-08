package no.nav.helse.testdata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.default
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticRootFolder
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.testdata.api.*
import no.nav.helse.testdata.rivers.VedtaksperiodeEndretRiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val env = setUpEnvironment()

    val httpClient = HttpClient(CIO) {
        expectSuccess = false
        install(ClientContentNegotiation) {
            jackson {
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                registerModule(JavaTimeModule())
            }
        }
    }

    val stsRestClient = StsRestClient("http://security-token-service.default.svc.nais.local", env.serviceUser)
    val inntektRestClient = InntektRestClient(env.inntektRestUrl, httpClient, stsRestClient)
    val aktørRestClient = AktørRestClient(env.aktørRestUrl, httpClient, stsRestClient)

    ApplicationBuilder(
        rapidsConfig = RapidApplication.RapidApplicationConfig.fromEnv(System.getenv()),
        subscriptionService = ConcreteSubscriptionService,
        aktørRestClient = aktørRestClient,
        inntektRestClient = inntektRestClient,
    ).start()
}

internal class ApplicationBuilder(
    rapidsConfig: RapidApplication.RapidApplicationConfig,
    private val subscriptionService: SubscriptionService,
    private val aktørRestClient: AktørRestClient,
    private val inntektRestClient: InntektRestClient,
) : RapidsConnection.StatusListener {
    private lateinit var rapidsMediator: RapidsMediator

    private val rapidsConnection =
        RapidApplication.Builder(rapidsConfig)
            .withKtorModule {
                installKtorModule(
                    subscriptionService,
                    aktørRestClient,
                    inntektRestClient,
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
    aktørRestClient: AktørRestClient,
    inntektRestClient: InntektRestClient,
    rapidsMediator: RapidsMediator,
) {
    installJacksonFeature()
    install(WebSockets)

    routing {
        registerPersonApi(rapidsMediator, aktørRestClient)
        registerVedtaksperiodeApi(rapidsMediator)
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

internal fun Application.installJacksonFeature() {
    install(ContentNegotiation) {
        jackson {
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            registerModule(JavaTimeModule())
        }
    }
}
