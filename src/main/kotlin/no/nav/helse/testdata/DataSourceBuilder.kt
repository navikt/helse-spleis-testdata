package no.nav.helse.testdata

import com.zaxxer.hikari.HikariConfig
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import javax.sql.DataSource

// Understands how to create a data source from environment variables
internal class DataSourceBuilder(env: Environment, databaseConfig: DatabaseConfig) {
    private val databaseName = databaseConfig.databaseName

    private val vaultMountPath = env.vaultMountPath

    // username and password is only needed when vault is not enabled,
    // since we rotate credentials automatically when vault is enabled
    private val hikariConfig = HikariConfig().apply {
        jdbcUrl = String.format(
            "jdbc:postgresql://%s:%s/%s%s",
            requireNotNull(databaseConfig.databaseHost) { "database host must be set if jdbc url is not provided" },
            requireNotNull(databaseConfig.databasePort) { "database port must be set if jdbc url is not provided" },
            requireNotNull(databaseConfig.databaseName) { "database name must be set if jdbc url is not provided" },
            databaseConfig.databaseUsername?.let { "?user=$it" } ?: "")
        username = env.serviceUser.username
        password = env.serviceUser.password

        maximumPoolSize = 3
        minimumIdle = 1
        idleTimeout = 10001
        connectionTimeout = 1000
        maxLifetime = 30001
    }

    fun getDataSource(role: Role = Role.User): DataSource {
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
            hikariConfig,
            vaultMountPath,
            "$databaseName-$role"
        )
    }

    enum class Role {
        Admin, User, ReadOnly;

        override fun toString() = name.toLowerCase()
    }
}
