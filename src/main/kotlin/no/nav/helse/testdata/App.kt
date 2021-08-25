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
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.*
import no.nav.helse.testdata.dokumenter.Vedtak
import no.nav.helse.testdata.dokumenter.inntektsmelding
import no.nav.helse.testdata.dokumenter.sykmelding
import no.nav.helse.testdata.dokumenter.søknad
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.AuthorizationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.YearMonth
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import kotlin.math.round

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())
const val spleisTopic = "tbd.rapid.v1"


@KtorExperimentalAPI
fun main() = runBlocking {
    val environment = setUpEnvironment()

    val spleisDataSource = DataSourceBuilder(environment, environment.databaseConfigs.spleisConfig).getDataSource()
    val spesialistDataSource = DataSourceBuilder(environment, environment.databaseConfigs.spesialistConfig).getDataSource()
    val spennDataSource = DataSourceBuilder(environment, environment.databaseConfigs.spennConfig).getDataSource()
    val rapidProducer = RapidProducer { KafkaProducer<String, String>(loadBaseConfig(environment).toProducerConfig()) }

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
        rapidProducer = rapidProducer
    )
}

internal class RapidProducer(private val producerFactory: () -> KafkaProducer<String, String>) {

    private var producer: KafkaProducer<String, String>? = null

    private fun producer(): KafkaProducer<String, String> {
        return (producer ?: producerFactory()).also {
            producer = it
        }
    }

    internal fun send(fnr: String, melding: String) {
        producer().send(ProducerRecord(spleisTopic, fnr, melding)) { _, err ->
            if (err != null && err is AuthorizationException) producer = null // nuller ut producer slik at den instansieres på nytt neste kall, med oppdatert autentisering
        }.get()
    }
}

internal fun launchApplication(
    spleisDataSource: DataSource,
    spesialistDataSource: DataSource,
    spennDataSource: DataSource,
    inntektRestClient: InntektRestClient,
    aktørRestClient: AktørRestClient,
    rapidProducer: RapidProducer
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
                registerVedtaksperiodeApi(rapidProducer, aktørRestClient)
                registerInntektsApi(inntektRestClient)
                registerBehovApi(rapidProducer)

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

internal fun Routing.registerVedtaksperiodeApi(
    producer: RapidProducer,
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

        sykmelding(vedtak, aktørId)?.also {
            log.info("produserer sykmelding på aktør: $aktørId\n$it")
            producer.send(vedtak.fnr, it)
        }

        søknad(vedtak, aktørId)?.also {
            log.info("produserer søknad på aktør: $aktørId\n$it")
            producer.send(vedtak.fnr, it)
        }

        inntektsmelding(vedtak, aktørId)?.also {
            log.info("produserer inntektsmelding på aktør: $aktørId\n$it")
            producer.send(vedtak.fnr, it)
        }

        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for vedtak på aktør: $aktørId") }
    }
}

internal fun Routing.registerBehovApi(
    producer: RapidProducer
) {
    post("/behov") {
        val behov = call.receive<ObjectNode>()
        behov.put("@event_name", "behov")
        if (!behov.path("@behov").isArray) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("fødselsnummer").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("organisasjonsnummer").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("vedtaksperiodeId").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        producer.send(behov.path("fødselsnummer").asText(), behov.toString())
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