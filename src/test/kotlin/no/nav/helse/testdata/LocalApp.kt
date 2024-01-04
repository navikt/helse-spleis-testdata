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
import java.time.LocalDate
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

    val aaregClient = mockk<AaregClient> {
        every {
            runBlocking { hentArbeidsforhold(any(), any()) }
        } returns listOf(
            AaregArbeidsforhold(
                type = Arbeidsforholdkode.ORDINÆRT,
                arbeidssted = Arbeidssted(Arbeidsstedtype.Underenhet, listOf(Ident(Identtype.ORGANISASJONSNUMMER, "111111111"))),
                ansettelsesperiode = Ansettelsesperiode(LocalDate.EPOCH, null),
                ansettelsesdetaljer = listOf(
                    Ansettelsesdetaljer(100, Yrke("10000", "UTVIKLER"), Ansettelseform("fast", "Fast stilling"), Rapporteringsmåneder(YearMonth.of(1970, 1), null))
                )
            )
        )
    }

    val eregClient = mockk<EregClient>() {
        every {
            runBlocking { hentOrganisasjon(any(), any()) }
        } returns EregResponse("Testnavn", emptyList())
    }

    val pdlClient = mockk<PdlClient>()

    val rapidsMediator = RapidsMediator(rapidsConnection)

    LocalApplicationBuilder(
        subscriptionService = LocalSubscriptionService,
        inntektRestClient = inntektRestClientMock,
        aaregClient = aaregClient,
        eregClient = eregClient,
        pdlClient = pdlClient,
        rapidsMediator = rapidsMediator,
    ).start()
}

internal class LocalApplicationBuilder(
    private val subscriptionService: SubscriptionService,
    private val inntektRestClient: InntektRestClient,
    private val aaregClient: AaregClient,
    private val eregClient: EregClient,
    private val pdlClient: PdlClient,
    private val rapidsMediator: RapidsMediator,
) : RapidsConnection.StatusListener {

    fun start() = runLocalServer {
        installKtorModule(
            subscriptionService = subscriptionService,
            inntektRestClient = inntektRestClient,
            aaregClient = aaregClient,
            eregClient = eregClient,
            pdlClient = pdlClient,
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
