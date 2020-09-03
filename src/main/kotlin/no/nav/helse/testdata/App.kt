package no.nav.helse.testdata

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.jackson.jackson
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())
const val spleisTopic = "helse-rapid-v1"


@KtorExperimentalAPI
fun main() = runBlocking {
    val environment = setUpEnvironment()

    val spleisDataSource = DataSourceBuilder(environment, environment.databaseConfigs.spleisConfig).getDataSource()
    val spesialistDataSource = DataSourceBuilder(environment, environment.databaseConfigs.spesialistConfig).getDataSource()
    val spennDataSource = DataSourceBuilder(environment, environment.databaseConfigs.spennConfig).getDataSource()
    val producer = KafkaProducer<String, String>(loadBaseConfig(environment).toProducerConfig())

    val httpClient = HttpClient(CIO) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = JacksonSerializer {
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                registerModule(JavaTimeModule())
            }
        }
    }

    val stsRestClient = StsRestClient("http://security-token-service.default.svc.nais.local", environment.serviceUser)
    val inntektRestClient = InntektRestClient(environment.inntektRestUrl, httpClient, stsRestClient)
    val aktørRestClient = AktørRestClient(environment.aktørRestUrl, httpClient, stsRestClient)

    launchApplication(
        spleisDataSource = spleisDataSource,
        spesialistDataSource = spesialistDataSource,
        spennDataSource = spennDataSource,
        inntektRestClient = inntektRestClient,
        aktørRestClient = aktørRestClient,
        producer = producer
    )
}

internal fun launchApplication(
    spleisDataSource: DataSource,
    spesialistDataSource: DataSource,
    spennDataSource: DataSource,
    inntektRestClient: InntektRestClient,
    aktørRestClient: AktørRestClient,
    producer: KafkaProducer<String, String>
) {
    val applicationContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    val exceptionHandler = CoroutineExceptionHandler { context, e ->
        log.error("Feil i lytter", e)
        context.cancel(CancellationException("Feil i lytter", e))
    }
    val personService = PersonService(
        spleisDataSource = spleisDataSource,
        spesialistDataSource = spesialistDataSource,
        spennDataSource = spennDataSource
    )


    runBlocking(exceptionHandler + applicationContext) {
        val server = embeddedServer(Netty, 8080) {
            install(MicrometerMetrics) {
                registry = meterRegistry
            }
            installJacksonFeature()

            routing {
                registerHealthApi({ true }, { true }, meterRegistry)
                registerPersonApi(personService, aktørRestClient)
                registerVedtaksperiodeApi(producer, aktørRestClient)
                registerInntektsApi(inntektRestClient)
                registerBehovApi(producer)

                static("/") {
                    staticRootFolder = File("public")
                    files("")
                    default("index.html")
                }
            }
        }.start(wait = false)

        Runtime.getRuntime().addShutdownHook(Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
            applicationContext.close()
        })
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
            val beregnetMånedsinntekt = beregnetÅrsinntekt / 12
            call.respond(
                mapOf(
                    "beregnetMånedsinntekt" to beregnetMånedsinntekt
                )
            )
        }
        is Result.Error -> call.respond(inntekterResult.error.statusCode, inntekterResult.error.response)
    }
}

internal fun Routing.registerVedtaksperiodeApi(
    producer: KafkaProducer<String, String>,
    aktørRestClient: AktørRestClient
) {
    post("/vedtaksperiode") {
        val vedtak = call.receive<Vedtak>()
        val aktørIdResult = aktørRestClient.hentAktørId(vedtak.fnr)

        if (aktørIdResult is Result.Error) {
            call.respond(HttpStatusCode.InternalServerError, aktørIdResult.error.message!!)
            return@post
        }
        val aktørId = aktørIdResult.unwrap()

        if (vedtak.skalSendeSykmelding) {
            val sykmelding = sykmelding(vedtak, aktørId)
            log.info("produserer sykmelding på aktør: $aktørId\n$sykmelding")
            producer.send(ProducerRecord(spleisTopic, vedtak.fnr, sykmelding)).get()
        }
        if (vedtak.skalSendeSøknad) {
            val søknad = søknad(vedtak, aktørId)
            log.info("produserer søknad på aktør: $aktørId\n$søknad")
            producer.send(ProducerRecord(spleisTopic, vedtak.fnr, søknad)).get()
        }
        if (vedtak.skalSendeInntektsmelding) {
            val inntektsmelding = inntektsmelding(vedtak, aktørId)
            log.info("produserer inntektsmelding på aktør: $aktørId\n$inntektsmelding")
            producer.send(ProducerRecord(spleisTopic, vedtak.fnr, inntektsmelding)).get()
        }

        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for vedtak på aktør: $aktørId") }
    }
}

internal fun Routing.registerBehovApi(
    producer: KafkaProducer<String, String>
) {
    post("/behov") {
        val behov = call.receive<ObjectNode>()
        behov.put("@event_name", "behov")
        if (!behov.path("@behov").isArray) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("fødselsnummer").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("organisasjonsnummer").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("vedtaksperiodeId").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        producer.send(ProducerRecord(spleisTopic, behov.toString())).get()
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Vedtak(
    val fnr: String,
    val orgnummer: String,
    val sykdomFom: LocalDate,
    val sykdomTom: LocalDate,
    val inntekt: Double,
    val harAndreInntektskilder: Boolean = false,
    val skalSendeInntektsmelding: Boolean = true,
    val skalSendeSykmelding: Boolean = true,
    val skalSendeSøknad: Boolean = true,
    val sykmeldingsgrad: Int = 100,
    val faktiskgrad: Int? = null,
    val sendtNav: LocalDate? = null,
    val sendtArbeidsgiver: LocalDate? = null,
    val førstefraværsdag: LocalDate?,
    val arbeidsgiverperiode: List<Periode>,
    val ferieperioder: List<Periode>,
    val opphørRefusjon: LocalDate? = null,
    val endringRefusjon: List<LocalDate> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Periode(val fom: LocalDate, val tom: LocalDate)
