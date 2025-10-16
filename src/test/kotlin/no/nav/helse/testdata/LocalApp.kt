package no.nav.helse.testdata

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.speed.SpeedClient
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

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

    val speedClient = mockk<SpeedClient>() {
        every { hentPersoninfo(any(), any()) } returns com.github.navikt.tbd_libs.speed.PersonResponse(
            fornavn = "NORMAL",
            mellomnavn = null,
            etternavn = "MUFFINS",
            fødselsdato = LocalDate.EPOCH,
            dødsdato = null,
            adressebeskyttelse = com.github.navikt.tbd_libs.speed.PersonResponse.Adressebeskyttelse.UGRADERT,
            kjønn = com.github.navikt.tbd_libs.speed.PersonResponse.Kjønn.UKJENT
        ).ok()
    }

    val rapidsMediator = RapidsMediator(object : RapidProducer {
        override fun publish(message: String) {
            rapidsConnection.publish(message)
        }

        override fun publish(key: String, message: String) {
            rapidsConnection.publish(key, message)
        }
    })

    LocalApplicationBuilder(
        subscriptionService = LocalSubscriptionService,
        inntektRestClient = inntektRestClientMock,
        aaregClient = aaregClient,
        eregClient = eregClient,
        speedClient = speedClient,
        rapidsMediator = rapidsMediator,
    ).start()
}

internal class LocalApplicationBuilder(
    private val subscriptionService: SubscriptionService,
    private val inntektRestClient: InntektRestClient,
    private val aaregClient: AaregClient,
    private val eregClient: EregClient,
    private val speedClient: SpeedClient,
    private val rapidsMediator: RapidsMediator,
) : RapidsConnection.StatusListener {

    fun start() = runLocalServer {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
                registerModule(JavaTimeModule())
            }
        }
        installKtorModule(
            subscriptionService = subscriptionService,
            inntektRestClient = inntektRestClient,
            aaregClient = aaregClient,
            eregClient = eregClient,
            speedClient = speedClient,
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
        val port = 8080
        log.info("Starter backend på port $port")
        val server = embeddedServer(CIO, port) {
            applicationBlock()
        }.start(wait = false)

        Runtime.getRuntime().addShutdownHook(Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
            applicationContext.close()
        })
    }
}
