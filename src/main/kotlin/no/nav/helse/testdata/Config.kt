package no.nav.helse.testdata

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

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
        databaseHost = System.getenv("DATABASE_HOST") ?: error("Mangler env var DATABASE_HOST"),
        databasePort = System.getenv("DATABASE_PORT") ?: error("Mangler env var DATABASE_PORT"),
        vaultMountPath = System.getenv("VAULT_MOUNTPATH") ?: error("Mangler env var VAULT_MOUNTPATH"),
        serviceUser = readServiceUserCredentials(),
        databaseUsername = System.getenv("DATABASE_USERNAME")
    )

data class Environment(
    val kafkaBootstrapServers: String,
    val databaseName: String,
    val databaseHost: String,
    val databasePort: String,
    val databaseUsername: String?,
    val vaultMountPath: String,
    val serviceUser: ServiceUser
)

data class ServiceUser(
    val username: String,
    val password: String
)

fun loadBaseConfig(env: Environment): Properties = Properties().also {
    it.load(Environment::class.java.getResourceAsStream("/kafka_base.properties"))
    it["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"${env.serviceUser.username}\" password=\"${env.serviceUser.password}\";"
    it["bootstrap.servers"] = env.kafkaBootstrapServers
}

fun Properties.toProducerConfig(): Properties = Properties().also {
    it.putAll(this)
    it[ConsumerConfig.GROUP_ID_CONFIG] = "spleis-testdata-producer"
    it[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    it[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
}
