package no.nav.helse.testdata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
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
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())
val spleisTopic = "helse-rapid-v1"


fun main() = runBlocking {
    val environment = setUpEnvironment()

    val dataSourceBuilder = DataSourceBuilder(environment)
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

    val stsRestClient = StsRestClient("http://security-token-service", environment.serviceUser)
    val inntektRestClient = InntektRestClient(environment.inntektRestUrl, httpClient, stsRestClient)
    val aktørRestClient = AktørRestClient(environment.aktørRestUrl, httpClient, stsRestClient)

    launchApplication(
        dataSourceBuilder.getDataSource(),
        inntektRestClient,
        aktørRestClient,
        producer
    )
}

internal fun launchApplication(
    dataSource: DataSource,
    inntektRestClient: InntektRestClient,
    aktørRestClient: AktørRestClient,
    producer: KafkaProducer<String, String>
) {
    val applicationContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    val exceptionHandler = CoroutineExceptionHandler { context, e ->
        log.error("Feil i lytter", e)
        context.cancel(CancellationException("Feil i lytter", e))
    }
    val personService = PersonService(dataSource)


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
            log.info("produserer sykmelding på aktør: $aktørId")
            val sykmelding = sykmelding(vedtak, aktørId)
            producer.send(ProducerRecord(spleisTopic, vedtak.fnr, sykmelding)).get()
        }
        if (vedtak.skalSendeSøknad) {
            log.info("produserer søknad på aktør: $aktørId")
            val søknad = søknad(vedtak, aktørId)
            producer.send(ProducerRecord(spleisTopic, vedtak.fnr, søknad)).get()
        }
        if (vedtak.skalSendeInntektsmelding) {
            val inntektsmelding = inntektsmelding(vedtak, aktørId)
            log.info("produserer inntektsmelding på aktør: $aktørId")
            producer.send(ProducerRecord(spleisTopic, vedtak.fnr, inntektsmelding)).get()
        }

        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for vedtak på aktør: $aktørId") }
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
    val sendtNav: LocalDate = sykdomTom.plusDays(1),
    val førstefraværsdag: LocalDate?,
    val arbeidsgiverperiode: List<Periode>,
    val ferieInntektsmelding: List<Periode>
)

data class Periode(val fom: LocalDate, val tom: LocalDate)
