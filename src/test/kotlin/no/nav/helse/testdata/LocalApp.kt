package no.nav.helse.testdata

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.*
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.testcontainers.containers.PostgreSQLContainer
import java.time.YearMonth
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

fun main() {
    val rapidsConnection = TestRapid()
    val psqlContainer = PostgreSQLContainer<Nothing>("postgres:12").apply {
        withInitScript("create_databases.sql")
        start()
    }

    val spleisDataSource = runMigration(psqlContainer, "spleis")
    val spesialistDataSource = runMigration(psqlContainer, "spesialist")
    val spennDataSource = runMigration(psqlContainer, "spenn")

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
        spleisDataSource = spleisDataSource,
        spesialistDataSource = spesialistDataSource,
        spennDataSource = spennDataSource
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

@Language("SQL")
private val dropTables = """
    DROP SCHEMA public CASCADE;
    CREATE SCHEMA public;
""".trimIndent()

fun runMigration(psql: PostgreSQLContainer<Nothing>, directory: String): DataSource {
    val dataSource = HikariDataSource(createHikariConfig(psql.withDatabaseName(directory)))
    Flyway.configure()
        .initSql(dropTables)
        .dataSource(dataSource)
        .locations("classpath:db/migration/$directory")
        .load()
        .migrate()
    return dataSource
}

fun createHikariConfig(psql: PostgreSQLContainer<Nothing>) =
    HikariConfig().apply {
        this.jdbcUrl = psql.jdbcUrl
        this.username = psql.username
        this.password = psql.password
        maximumPoolSize = 3
        minimumIdle = 1
        idleTimeout = 10001
        connectionTimeout = 1000
        maxLifetime = 30001
    }
