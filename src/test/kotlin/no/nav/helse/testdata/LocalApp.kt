package no.nav.helse.testdata

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

fun main() {
    val embeddedPostgres = EmbeddedPostgres.builder().start()

    runMigration(embeddedPostgres)
    launchApplication(embeddedPostgres.postgresDatabase)
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