package no.nav.helse.testdata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.testdata.api.registerBehovApi
import no.nav.helse.testdata.api.registerDollyApi
import no.nav.helse.testdata.api.registerSubscriptionApi
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

    val dollyRestClient = DollyRestClient(env.dollyRestUrl, httpClient)

    ApplicationBuilder(
        rapidsConfig = RapidApplication.RapidApplicationConfig.fromEnv(System.getenv()),
        subscriptionService = ConcreteSubscriptionService,
        dollyRestClient = dollyRestClient,
        azureConfig = env.azureADConfig,
    ).start()
}

internal class ApplicationBuilder(
    rapidsConfig: RapidApplication.RapidApplicationConfig,
    private val subscriptionService: SubscriptionService,
    private val dollyRestClient: DollyRestClient,
    private val azureConfig: AzureADConfig,
) : RapidsConnection.StatusListener {
    private lateinit var rapidsMediator: RapidsMediator

    private val rapidsConnection =
        RapidApplication.Builder(rapidsConfig)
            .withKtorModule {
                installKtorModule(
                    subscriptionService = subscriptionService,
                    dollyRestClient = dollyRestClient,
                    rapidsMediator = rapidsMediator,
                    azureConfig = azureConfig,
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
    dollyRestClient: DollyRestClient,
    rapidsMediator: RapidsMediator,
    azureConfig: AzureADConfig,
) {
    installJacksonFeature()

    install(WebSockets)

    install(Authentication) {
        jwt("oidc") {
            azureConfig.configureAuthentication(this)
        }
    }

    install(Sessions) {
        cookie<UserSession>("testdata", storage = SessionStorageMemory())
    }

    routing {
        authenticate("oidc") {
            registerDollyApi(dollyRestClient)
            registerBehovApi(rapidsMediator)
            registerSubscriptionApi(subscriptionService)

            static("/") {
                handle {
                    val principal = call.principal<JWTPrincipal>()
                    no.nav.helse.testdata.log.info(principal?.payload?.issuer)
                    no.nav.helse.testdata.log.info(principal?.payload?.subject)
                    no.nav.helse.testdata.log.info(principal?.payload?.audience.toString())
                    no.nav.helse.testdata.log.info(principal?.payload?.claims.toString())
                }
                staticRootFolder = File("public")
                files("")
                default("index.html")
            }
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

data class UserSession(val accessToken: String?) : Principal