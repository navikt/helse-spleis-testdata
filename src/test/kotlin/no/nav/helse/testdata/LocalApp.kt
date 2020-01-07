package no.nav.helse.testdata

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.mockk.mockk
import org.apache.kafka.clients.producer.KafkaProducer
import org.flywaydb.core.Flyway

fun main() {
    val embeddedPostgres = EmbeddedPostgres.builder().start()
    val producer: KafkaProducer<String, String> = mockk(relaxed = true)

    runMigration(embeddedPostgres)
    launchApplication(embeddedPostgres.postgresDatabase, producer)
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
