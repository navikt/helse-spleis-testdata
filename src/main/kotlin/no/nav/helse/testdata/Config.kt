package no.nav.helse.testdata

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64

const val vaultBase = "/var/run/secrets/nais.io/vault"
val vaultBasePath: Path = Paths.get(vaultBase)

fun readServiceUserCredentials() = ServiceUser(
    username = Files.readString(vaultBasePath.resolve("username")),
    password = Files.readString(vaultBasePath.resolve("password"))
)

fun setUpEnvironment() =
    Environment(
        kafkaBootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: error("Mangler env var KAFKA_BOOTSTRAP_SERVERS"),
        databaseName = System.getenv("DATABASE_NAME") ?: error("Mangler env var DATABASE_NAME"),
        databaseUrl = System.getenv("DATABASE_JDBC_URL") ?: error("Mangler env var DATABASE_JDBC_URL"),
        vaultMountPath = System.getenv("VAULT_MOUNTPATH") ?: error("Mangler env var VAULT_MOUNTPATH"),
        serviceUser = readServiceUserCredentials()
    )

data class Environment(
    val kafkaBootstrapServers: String,
    val databaseName: String,
    val databaseUrl: String,
    val vaultMountPath: String,
    val serviceUser: ServiceUser
)

data class ServiceUser(
    val username: String,
    val password: String
) {
    val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
}
