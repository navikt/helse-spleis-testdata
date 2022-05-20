package no.nav.helse.testdata

internal fun setUpEnvironment() =
    Environment(
        kafkaBrokers = System.getenv("KAFKA_BROKERS") ?: error("Mangler env var KAFKA_BROKERS"),
        kafkaCredstorePassword = System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        kafkaTruststorePath = System.getenv("KAFKA_TRUSTSTORE_PATH"),
        kafkaKeystorePath = System.getenv("KAFKA_KEYSTORE_PATH"),
        dollyRestUrl = "https://dolly-backend.dev.intern.nav.no/api/v1",
    )

data class Environment(
    val kafkaBrokers: String,
    val kafkaCredstorePassword: String,
    val kafkaTruststorePath: String,
    val kafkaKeystorePath: String,
    val dollyRestUrl: String,
)
