package no.nav.helse.testdata

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64
import java.util.Properties

const val vaultBase = "/var/run/secrets/nais.io/vault"
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
        kafkaBootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS")
            ?: error("Mangler env var KAFKA_BOOTSTRAP_SERVERS"),
        databaseConfigs = DatabaseConfigs(
            spleisConfig = getDatabaseEnv("SPLEIS"),
            spesialistConfig = getDatabaseEnv("SPESIALIST"),
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
    val databaseUsername: String?
)

data class DatabaseConfigs(
    val spleisConfig: DatabaseConfig,
    val spesialistConfig: DatabaseConfig,
    val spennConfig: DatabaseConfig
)

data class Environment(
    val kafkaBootstrapServers: String,
    val databaseConfigs: DatabaseConfigs,
    val vaultMountPath: String,
    val inntektRestUrl: String,
    val aktørRestUrl: String,
    val serviceUser: ServiceUser
)

data class ServiceUser(
    val username: String,
    val password: String
) {
    val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
}

fun loadBaseConfig(env: Environment): Properties = Properties().also {
    it.load(Environment::class.java.getResourceAsStream("/kafka_base.properties"))
    it["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"${env.serviceUser.username}\" password=\"${env.serviceUser.password}\";"
    it["bootstrap.servers"] = env.kafkaBootstrapServers
}

fun Properties.toProducerConfig(): Properties = Properties().also {
    it.putAll(this)
    it[ConsumerConfig.GROUP_ID_CONFIG] = "spleis-testdata-v1"
    it[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    it[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
}
