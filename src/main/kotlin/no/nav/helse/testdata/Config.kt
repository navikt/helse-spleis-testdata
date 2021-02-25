package no.nav.helse.testdata

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringSerializer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64
import java.util.Properties

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
    val kafkaBrokers: String,
    val databaseConfigs: DatabaseConfigs,
    val vaultMountPath: String,
    val inntektRestUrl: String,
    val aktørRestUrl: String,
    val serviceUser: ServiceUser,
    val kafkaCredstorePassword: String,
    val kafkaTruststorePath: String,
    val kafkaKeystorePath: String
)

data class ServiceUser(
    val username: String,
    val password: String
) {
    val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
}

fun loadBaseConfig(env: Environment): Properties = Properties().apply {
    put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.kafkaBrokers)
    put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name)
    put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
    put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
    put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
    put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, env.kafkaTruststorePath)
    put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, env.kafkaCredstorePassword)
    put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, env.kafkaKeystorePath)
    put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, env.kafkaCredstorePassword)
}

fun Properties.toProducerConfig(): Properties = Properties().apply {
    putAll(this@toProducerConfig)
    this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    put(ProducerConfig.ACKS_CONFIG, "1")
    put(ProducerConfig.LINGER_MS_CONFIG, "0")
    put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
}
