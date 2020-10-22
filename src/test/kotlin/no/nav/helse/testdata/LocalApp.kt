package no.nav.helse.testdata

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.flywaydb.core.Flyway
import java.time.YearMonth

fun main() {
    val spleisDB = EmbeddedPostgres.builder().start()
    val spesialistDB = EmbeddedPostgres.builder().start()
    val spennDB = EmbeddedPostgres.builder().start()
    val producer: KafkaProducer<String, String> = mockk(relaxed = true)

    runMigration(spleisDB, "spleis")
    runMigration(spesialistDB, "spesialist")
    runMigration(spennDB, "spenn")
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
    val aktørRestClientMock =
        mockk<AktørRestClient> {
            every { runBlocking { hentAktørId(any()) } }.returns(Result.Ok("aktørId"))
        }
    launchApplication(
        spleisDataSource = spleisDB.postgresDatabase,
        spesialistDataSource = spesialistDB.postgresDatabase,
        spennDataSource = spennDB.postgresDatabase,
        inntektRestClient = inntektRestClientMock,
        aktørRestClient = aktørRestClientMock,
        producer = producer
    )
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
