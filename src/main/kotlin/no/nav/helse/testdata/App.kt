package no.nav.helse.testdata

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.default
import io.ktor.http.content.file
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.jackson.jackson
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val log: Logger = LoggerFactory.getLogger("spleis-testdata")
val spleisTopic = "privat-helse-sykepenger-rapid"


fun main() = runBlocking {
    val environment = setUpEnvironment()

    val dataSourceBuilder = DataSourceBuilder(environment)
    val producer = KafkaProducer<String, String>(loadBaseConfig(environment).toProducerConfig())

    launchApplication(
        dataSourceBuilder.getDataSource(),
        producer
    )
}

fun launchApplication(
    dataSource: DataSource,
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
                registerPersonApi(personService)
                registerVedtaksperiodeApi(producer)

                static("/") {
                    staticRootFolder = File("public")
                    file("styles.css")
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

fun Routing.registerPersonApi(personService: PersonService) {
    delete("person/{aktørId}") {
        personService.slett(call.parameters["aktørId"] ?: throw IllegalArgumentException("Mangler aktørid"))
        call.respond(HttpStatusCode.OK)
    }
}

fun Routing.registerVedtaksperiodeApi(producer: KafkaProducer<String, String>) {
    post("vedtaksperiode") {
        val vedtak = call.receive<Vedtak>()

        val sykmelding = sykmelding(vedtak)
        val søknad = søknad(vedtak)
        val inntektsmelding = inntektsmelding(vedtak)

        producer.send(ProducerRecord(spleisTopic, vedtak.aktørId, sykmelding))
        producer.send(ProducerRecord(spleisTopic, vedtak.aktørId, søknad))
        producer.send(ProducerRecord(spleisTopic, vedtak.aktørId, inntektsmelding))

        call.respond(HttpStatusCode.OK)
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
    val aktørId: String,
    val fnr: String,
    val orgnummer: String,
    val sykdomFom: LocalDate,
    val sykdomTom: LocalDate
)