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

internal fun setUpEnvironment() =
    Environment(
        kafkaBrokers = System.getenv("KAFKA_BROKERS") ?: error("Mangler env var KAFKA_BROKERS"),
        kafkaCredstorePassword = System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        kafkaTruststorePath = System.getenv("KAFKA_TRUSTSTORE_PATH"),
        kafkaKeystorePath = System.getenv("KAFKA_KEYSTORE_PATH"),
        vaultMountPath = System.getenv("VAULT_MOUNTPATH") ?: error("Mangler env var VAULT_MOUNTPATH"),
        inntektRestUrl = "https://app-q1.adeo.no/inntektskomponenten-ws/rs",
        serviceUser = readServiceUserCredentials(),
        aktørRestUrl = "https://app-q1.adeo.no/aktoerregister/api/v1",
        dollyRestUrl = "https://dolly-backend.dev.intern.nav.no/api/v1"
    )

data class Environment(
    val kafkaBrokers: String,
    val vaultMountPath: String,
    val inntektRestUrl: String,
    val aktørRestUrl: String,
    val serviceUser: ServiceUser,
    val kafkaCredstorePassword: String,
    val kafkaTruststorePath: String,
    val kafkaKeystorePath: String,
    val dollyRestUrl: String,
)

data class ServiceUser(
    val username: String,
    val password: String,
) {
    val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
}
