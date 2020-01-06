package no.nav.helse.testdata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.install
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val log: Logger = LoggerFactory.getLogger("spleis-faketestdata")

@FlowPreview
fun main() = runBlocking {
    val serviceUser = readServiceUserCredentials()
    val environment = setUpEnvironment()

    launchApplication()
}

@FlowPreview
fun launchApplication() {
    val applicationContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    val exceptionHandler = CoroutineExceptionHandler { context, e ->
        log.error("Feil i lytter", e)
        context.cancel(CancellationException("Feil i lytter", e))
    }
    runBlocking(exceptionHandler + applicationContext) {
        val server = embeddedServer(Netty, 8080) {
            install(MicrometerMetrics) {
                registry = meterRegistry
            }

            routing {
                registerHealthApi({ true }, { true }, meterRegistry)
            }
        }.start(wait = false)

        Runtime.getRuntime().addShutdownHook(Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
            applicationContext.close()
        })
    }
}
