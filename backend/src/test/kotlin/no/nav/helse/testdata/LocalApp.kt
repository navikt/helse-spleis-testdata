package no.nav.helse.testdata

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.*
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import java.time.YearMonth
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val rapidsConnection = TestRapid()

    val inntektRestClientMock = mockk<InntektRestClient> {
        every { runBlocking { hentInntektsliste(any(), any(), any(), any(), any()) } }.returns(
            Result.Ok(
                (1..12).map {
                    Måned(
                        YearMonth.of(2019, it), listOf(
                            Inntekt(30000.0, Inntektstype.LOENNSINNTEKT, "123456789"),
                            Inntekt(30000.0, Inntektstype.LOENNSINNTEKT, "987654321")
                        )
                    )
                }
            )
        )
    }

    val rapidsMediator = RapidsMediator(rapidsConnection)

    LocalApplicationBuilder(
        subscriptionService = LocalSubscriptionService,
        inntektRestClient = inntektRestClientMock,
        rapidsMediator = rapidsMediator,
    ).start()
}

internal class LocalApplicationBuilder(
    private val subscriptionService: SubscriptionService,
    private val inntektRestClient: InntektRestClient,
    private val rapidsMediator: RapidsMediator,
) : RapidsConnection.StatusListener {

    fun start() = runLocalServer {
        installKtorModule(
            subscriptionService = subscriptionService,
            inntektRestClient = inntektRestClient,
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
