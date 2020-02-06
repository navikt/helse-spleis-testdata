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
    val embeddedPostgres = EmbeddedPostgres.builder().start()
    val producer: KafkaProducer<String, String> = mockk(relaxed = true)

    runMigration(embeddedPostgres)
    launchApplication(embeddedPostgres.postgresDatabase, mockk {
        every { runBlocking { hentInntektsliste(any(), any(), any(), any(), any()) } }.returns(
            Result.Ok(
                (1..12).map {
                    Måned(
                        YearMonth.of(2019, it), listOf(Inntekt(30000.0, Inntektstype.LOENNSINNTEKT, "orgnummer"))
                    )
                }
            )
        )
    }, mockk(), producer)
}

fun runMigration(embeddedPostgres: EmbeddedPostgres) =
    Flyway.configure()
        .dataSource(HikariDataSource(createHikariConfig(embeddedPostgres.getJdbcUrl("postgres", "postgres"))))
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
