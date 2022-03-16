package no.nav.helse.testdata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.websocket.*
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.testdata.api.*
import no.nav.helse.testdata.rivers.VedtaksperiodeEndretRiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.sql.DataSource

val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val env = setUpEnvironment()

    val spesialistDataSource = DataSourceBuilder(env, env.databaseConfigs.spesialistConfig).getDataSource()
    val spennDataSource = DataSourceBuilder(env, env.databaseConfigs.spennConfig).getDataSource()

    val httpClient = HttpClient(CIO) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = JacksonSerializer {
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
        spesialistDataSource = spesialistDataSource,
        spennDataSource = spennDataSource,
        subscriptionService = ConcreteSubscriptionService,
        aktørRestClient = aktørRestClient,
        inntektRestClient = inntektRestClient
    ).start()
}

internal class ApplicationBuilder(
    rapidsConfig: RapidApplication.RapidApplicationConfig,
    spesialistDataSource: DataSource,
    spennDataSource: DataSource,
    private val subscriptionService: SubscriptionService,
    private val aktørRestClient: AktørRestClient,
    private val inntektRestClient: InntektRestClient,
) : RapidsConnection.StatusListener {
    private lateinit var rapidsMediator: RapidsMediator
    private lateinit var personService: PersonService

    private val rapidsConnection =
        RapidApplication.Builder(rapidsConfig)
            .withKtorModule {
                installKtorModule(
                    personService,
                    subscriptionService,
                    aktørRestClient,
                    inntektRestClient,
                    rapidsMediator
                )
            }.build()

    init {
        rapidsMediator = RapidsMediator(rapidsConnection)
        personService = PersonService(spesialistDataSource, spennDataSource, rapidsMediator)
        rapidsConnection.register(this)
        VedtaksperiodeEndretRiver(rapidsConnection, subscriptionService)
    }

    fun start() = rapidsConnection.start()
}

internal fun Application.installKtorModule(
    personService: PersonService,
    subscriptionService: SubscriptionService,
    aktørRestClient: AktørRestClient,
    inntektRestClient: InntektRestClient,
    rapidsMediator: RapidsMediator,
) {
    installJacksonFeature()
    install(WebSockets)

    routing {
        registerPersonApi(personService, aktørRestClient)
        registerVedtaksperiodeApi(rapidsMediator, aktørRestClient)
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
