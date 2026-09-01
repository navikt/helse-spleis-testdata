package no.nav.helse.testdata

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.speed.SpeedClient
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import no.nav.helse.testdata.db.ForsikringReplikaTestdataDao
import org.flywaydb.core.Flyway
import org.testcontainers.postgresql.PostgreSQLContainer

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

    val postgres = PostgreSQLContainer("postgres:17").apply { start() }
    val forsikringReplikaTestdataDataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username
            password = postgres.password
            maximumPoolSize = 5
            poolName = "forsikring-replika-testdata-lokalt"
        }
    )
    Flyway
        .configure()
        .dataSource(forsikringReplikaTestdataDataSource)
        .locations("classpath:forsikring-replika-testdata/db/migrations")
        .load()
        .migrate()

    LocalApplicationBuilder(
        subscriptionService = LocalSubscriptionService,
        inntektRestClient = inntektRestClientMock,
        aaregClient = aaregClient,
        eregClient = eregClient,
        speedClient = speedClient,
        rapidsMediator = rapidsMediator,
        forsikringReplikaTestdataDao = ForsikringReplikaTestdataDao(forsikringReplikaTestdataDataSource),
    ).start()
}

internal class LocalApplicationBuilder(
    private val subscriptionService: SubscriptionService,
    private val inntektRestClient: InntektRestClient,
    private val aaregClient: AaregClient,
    private val eregClient: EregClient,
    private val speedClient: SpeedClient,
    private val rapidsMediator: RapidsMediator,
    private val forsikringReplikaTestdataDao: ForsikringReplikaTestdataDao,
) : RapidsConnection.StatusListener {

    fun start() = runLocalServer {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
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
            forsikringReplikaTestdataDao = forsikringReplikaTestdataDao,
        )
    }
}

internal fun runLocalServer(applicationBlock: Application.() -> Unit) {
    val port = 8080
    log.info("Starter backend på port $port")
    embeddedServer(CIO, port) {
        applicationBlock()
    }.start(wait = true)
}
