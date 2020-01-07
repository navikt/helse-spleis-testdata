package no.nav.helse.testdata

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.IllegalArgumentException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val log: Logger = LoggerFactory.getLogger("spleis-testdata")

@FlowPreview
fun main() = runBlocking {
    val environment = setUpEnvironment()

    val dataSourceBuilder = DataSourceBuilder(environment)

    launchApplication(dataSourceBuilder.getDataSource())
}

fun launchApplication(dataSource: DataSource) {
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

            routing {
                registerHealthApi({ true }, { true }, meterRegistry)
                registerPersonApi(personService)

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
