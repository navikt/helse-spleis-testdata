package no.nav.helse.testdata

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.*
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.flywaydb.core.Flyway
import java.time.YearMonth
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val spleisDB = EmbeddedPostgres.builder().start()
    val spesialistDB = EmbeddedPostgres.builder().start()
    val spennDB = EmbeddedPostgres.builder().start()

    runMigration(spleisDB, "spleis")
    runMigration(spesialistDB, "spesialist")
    runMigration(spennDB, "spenn")

    val aktørRestClientMock =
        mockk<AktørRestClient> {
            every { runBlocking { hentAktørId(any()) } }.returns(Result.Ok("aktørId"))
        }

    val inntektRestClientMock = mockk<InntektRestClient> {
        every { runBlocking { hentInntektsliste(any(), any(), any(), any(), any()) } }.returns(
            Result.Ok(
                (1..12).map {
                    Måned(
                        YearMonth.of(2019, it), listOf(Inntekt(30000.0, Inntektstype.LOENNSINNTEKT, "orgnummer"))
                    )
                }
            )
        )
    }

    val personService = PersonService(
        spleisDataSource = spleisDB.postgresDatabase,
        spesialistDataSource = spesialistDB.postgresDatabase,
        spennDataSource = spennDB.postgresDatabase
    )

    LocalApplicationBuilder(
        personService = personService,
        subscriptionService = LocalSubscriptionService,
        aktørRestClient = aktørRestClientMock,
        inntektRestClient = inntektRestClientMock,
    ).start()
}

internal class LocalApplicationBuilder(
    private val personService: PersonService,
    private val subscriptionService: SubscriptionService,
    private val aktørRestClient: AktørRestClient,
    private val inntektRestClient: InntektRestClient,
) : RapidsConnection.StatusListener {
    private val rapidsConnection = TestRapid()
    private val rapidsMediator = RapidsMediator(rapidsConnection)

    fun start() = runLocalServer {
        installKtorModule(
            personService = personService,
            subscriptionService = subscriptionService,
            aktørRestClient = aktørRestClient,
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

fun runMigration(embeddedPostgres: EmbeddedPostgres, directory: String) =
    Flyway.configure()
        .dataSource(HikariDataSource(createHikariConfig(embeddedPostgres.getJdbcUrl("postgres", "postgres"))))
        .locations("classpath:db/migration/$directory")
        .load()
        .migrate()

fun createHikariConfig(jdbcUrl: String) =
    HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        maximumPoolSize = 3
        minimumIdle = 1
        idleTimeout = 10001
        connectionTimeout = 1000
        maxLifetime = 30001
    }
