package no.nav.helse.testdata

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

const val vaultBase = "/var/run/secrets/nais.io/service_user"
val vaultBasePath: Path = Paths.get(vaultBase)

private fun readServiceUserCredentials() = ServiceUser(
    username = Files.readString(vaultBasePath.resolve("username")),
    password = Files.readString(vaultBasePath.resolve("password"))
)

private fun getDatabaseEnv(postfix: String) = DatabaseConfig(
    databaseName = System.getenv("DATABASE_NAME_$postfix") ?: error("Mangler env var DATABASE_NAME_$postfix"),
    databaseHost = System.getenv("DATABASE_HOST_$postfix") ?: error("Mangler env var DATABASE_HOST_$postfix"),
    databasePort = System.getenv("DATABASE_PORT_$postfix") ?: error("Mangler env var DATABASE_PORT_$postfix"),
    databaseUsername = System.getenv("DATABASE_USERNAME_$postfix")
)

internal fun setUpEnvironment() =
    Environment(
        kafkaBrokers = System.getenv("KAFKA_BROKERS") ?: error("Mangler env var KAFKA_BROKERS"),
        kafkaCredstorePassword = System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        kafkaTruststorePath = System.getenv("KAFKA_TRUSTSTORE_PATH"),
        kafkaKeystorePath = System.getenv("KAFKA_KEYSTORE_PATH"),
        databaseConfigs = DatabaseConfigs(
            spennConfig = getDatabaseEnv("SPENN")
        ),
        vaultMountPath = System.getenv("VAULT_MOUNTPATH") ?: error("Mangler env var VAULT_MOUNTPATH"),
        inntektRestUrl = "https://app-q1.adeo.no/inntektskomponenten-ws/rs",
        serviceUser = readServiceUserCredentials(),
        aktørRestUrl = "https://app-q1.adeo.no/aktoerregister/api/v1"
    )

data class DatabaseConfig(
    val databaseName: String,
    val databaseHost: String,
    val databasePort: String,
    val databaseUsername: String?,
)

data class DatabaseConfigs(
    val spennConfig: DatabaseConfig,
)

data class Environment(
    val kafkaBrokers: String,
    val databaseConfigs: DatabaseConfigs,
    val vaultMountPath: String,
    val inntektRestUrl: String,
    val aktørRestUrl: String,
    val serviceUser: ServiceUser,
    val kafkaCredstorePassword: String,
    val kafkaTruststorePath: String,
    val kafkaKeystorePath: String,
)

data class ServiceUser(
    val username: String,
    val password: String,
) {
    val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
}
