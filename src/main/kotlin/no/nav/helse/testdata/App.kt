package no.nav.helse.testdata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.jackson.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.testdata.dokumenter.Vedtak
import no.nav.helse.testdata.dokumenter.inntektsmelding
import no.nav.helse.testdata.dokumenter.sykmelding
import no.nav.helse.testdata.dokumenter.søknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.YearMonth
import java.util.*
import kotlin.math.round

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

@KtorExperimentalAPI
fun main() {
    val env = setUpEnvironment()

    val spleisDataSource = DataSourceBuilder(env, env.databaseConfigs.spleisConfig).getDataSource()
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

    val personService = PersonService(
        spleisDataSource = spleisDataSource,
        spesialistDataSource = spesialistDataSource,
        spennDataSource = spennDataSource
    )

    ApplicationBuilder(
        rapidsConfig = RapidApplication.RapidApplicationConfig.fromEnv(System.getenv()),
        personService = personService,
        aktørRestClient = aktørRestClient,
        inntektRestClient = inntektRestClient
    ).start()
}

internal data class RapidsMediator(internal val connection: RapidsConnection)

internal class ApplicationBuilder(
    rapidsConfig: RapidApplication.RapidApplicationConfig,
    private val personService: PersonService,
    private val aktørRestClient: AktørRestClient,
    private val inntektRestClient: InntektRestClient,
) : RapidsConnection.StatusListener {
    private lateinit var rapidsMediator: RapidsMediator

    private val rapidsConnection =
        RapidApplication.Builder(rapidsConfig)
            .withKtorModule { installKtorModule(personService, aktørRestClient, inntektRestClient, rapidsMediator) }
            .build()

    init {
        rapidsMediator = RapidsMediator(rapidsConnection)
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
}

internal fun Application.installKtorModule(
    personService: PersonService,
    aktørRestClient: AktørRestClient,
    inntektRestClient: InntektRestClient,
    rapidsMediator: RapidsMediator,
) {
    install(MicrometerMetrics) {
        registry = meterRegistry
    }
    installJacksonFeature()

    routing {
        registerHealthApi({ true }, { true }, meterRegistry)
        registerPersonApi(personService, aktørRestClient)
        registerVedtaksperiodeApi(rapidsMediator, aktørRestClient)
        registerInntektsApi(inntektRestClient)
        registerBehovApi(rapidsMediator)

        static("/") {
            staticRootFolder = File("public")
            files("")
            default("index.html")
        }
    }
}

internal fun Routing.registerPersonApi(personService: PersonService, aktørRestClient: AktørRestClient) {
    delete("/person") {
        val fnr = call.request.header("ident")
        personService.slett(fnr ?: throw IllegalArgumentException("Mangler ident"))
        call.respond(HttpStatusCode.OK)
    }
    get("/person/aktorid") {
        val fnr = call.request.header("ident")
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Mangler ident i requesten")

        return@get when (val res = aktørRestClient.hentAktørId(fnr)) {
            is Result.Ok -> call.respond(HttpStatusCode.OK, res.value)
            is Result.Error -> call.respond(HttpStatusCode.InternalServerError, "Feil")
        }
    }
}

internal fun Routing.registerInntektsApi(inntektRestClient: InntektRestClient) = get("/person/inntekt") {
    val fnr = requireNotNull(call.request.header("ident")) { "Mangler header: [ident: fnr]" }
    val end = YearMonth.now().minusMonths(1)
    val start = end.minusMonths(11)
    val inntekterResult = inntektRestClient.hentInntektsliste(
        fnr = fnr,
        fom = start,
        tom = end,
        filter = "8-30",
        callId = UUID.randomUUID().toString()
    )
    when (inntekterResult) {
        is Result.Ok -> {
            val beregnetÅrsinntekt = inntekterResult.value.flatMap { it.inntektsliste }.sumByDouble { it.beløp }
            val beregnetMånedsinntekt = round(beregnetÅrsinntekt / 12)
            call.respond(
                mapOf(
                    "beregnetMånedsinntekt" to beregnetMånedsinntekt
                )
            )
        }
        is Result.Error -> call.respond(inntekterResult.error.statusCode, inntekterResult.error.response)
    }
}

internal fun Routing.registerVedtaksperiodeApi(mediator: RapidsMediator, aktørRestClient: AktørRestClient) {
    post("/vedtaksperiode") {
        val vedtak = call.receive<Vedtak>()
        val aktørIdResult = aktørRestClient.hentAktørId(vedtak.fnr)

        if (aktørIdResult is Result.Error) {
            call.respond(HttpStatusCode.InternalServerError, aktørIdResult.error.message!!)
            return@post
        }
        val aktørId = aktørIdResult.unwrap()

        sykmelding(vedtak, aktørId)?.also {
            log.info("produserer sykmelding på aktør: $aktørId\n$it")
            mediator.connection.publish(vedtak.fnr, it)
        }

        søknad(vedtak, aktørId)?.also {
            log.info("produserer søknad på aktør: $aktørId\n$it")
            mediator.connection.publish(vedtak.fnr, it)
        }

        inntektsmelding(vedtak, aktørId)?.also {
            log.info("produserer inntektsmelding på aktør: $aktørId\n$it")
            mediator.connection.publish(vedtak.fnr, it)
        }

        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for vedtak på aktør: $aktørId") }
    }
}

internal fun Routing.registerBehovApi(mediator: RapidsMediator) {
    post("/behov") {
        val behov = call.receive<ObjectNode>()
        behov.put("@event_name", "behov")
        if (!behov.path("@behov").isArray) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("fødselsnummer").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("organisasjonsnummer").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("vedtaksperiodeId").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        mediator.connection.publish(behov.path("fødselsnummer").asText(), behov.toString())
        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for behov: $behov") }
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