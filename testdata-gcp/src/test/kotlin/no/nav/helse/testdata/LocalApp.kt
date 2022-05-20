package no.nav.helse.testdata

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.mockk.mockk
import kotlinx.coroutines.*
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val rapidsConnection = TestRapid()

    val dollyRestClient = mockk<DollyRestClient>(relaxed = true)

    val rapidsMediator = RapidsMediator(rapidsConnection)

    LocalApplicationBuilder(
        subscriptionService = LocalSubscriptionService,
        dollyRestClient = dollyRestClient,
        rapidsMediator = rapidsMediator,
    ).start()
}

internal class LocalApplicationBuilder(
    private val subscriptionService: SubscriptionService,
    private val dollyRestClient: DollyRestClient,
    private val rapidsMediator: RapidsMediator,
) : RapidsConnection.StatusListener {

    fun start() = runLocalServer {
        installKtorModule(
            subscriptionService = subscriptionService,
            dollyRestClient = dollyRestClient,
            rapidsMediator = rapidsMediator,
        )
    }
}

internal fun runLocalServer(applicationBlock: Application.() -> Unit) {
    val applicationContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    val exceptionHandler = CoroutineExceptionHandler { context, e ->
        log.error("Feil i lytter", e)
        context.cancel(CancellationException("Feil i lytter", e))
    }

    runBlocking(exceptionHandler + applicationContext) {
        val server = embeddedServer(Netty, 8080) {
            applicationBlock()
        }.start(wait = false)

        Runtime.getRuntime().addShutdownHook(Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
            applicationContext.close()
        })
    }
}