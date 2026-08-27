package no.nav.helse.testdata.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.helse.testdata.Environment
import org.flywaydb.core.Flyway

internal object ForsikringReplikaTestdataDataSource {
    fun createDataSource(env: Environment): HikariDataSource =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = env.databaseJdbcUrl
                username = env.databaseUsername
                password = env.databasePassword
                maximumPoolSize = 5
                poolName = "forsikring-replika-testdata"
            }
        )

    fun migrate(dataSource: HikariDataSource) {
        Flyway
            .configure()
            .dataSource(dataSource)
            .locations("classpath:forsikring-replika-testdata/db/migrations")
            .load()
            .migrate()
    }
}
