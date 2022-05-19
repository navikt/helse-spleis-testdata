package no.nav.helse.testdata

import com.fasterxml.jackson.databind.JsonNode
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

internal fun setUpEnvironment() =
    Environment(
        kafkaBrokers = System.getenv("KAFKA_BROKERS") ?: error("Mangler env var KAFKA_BROKERS"),
        kafkaCredstorePassword = System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        kafkaTruststorePath = System.getenv("KAFKA_TRUSTSTORE_PATH"),
        kafkaKeystorePath = System.getenv("KAFKA_KEYSTORE_PATH"),
        dollyRestUrl = "https://dolly-backend.dev.intern.nav.no/api/v1",
        azureADConfig = AzureADConfig(
            discoveryUrl = System.getenv("AZURE_APP_WELL_KNOWN_URL"),
            clientId = System.getenv("AZURE_APP_CLIENT_ID"),
            clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET"),
            authorizationUrl = System.getenv("AUTHORIZATION_URL"),
        )
    )

data class Environment(
    val kafkaBrokers: String,
    val kafkaCredstorePassword: String,
    val kafkaTruststorePath: String,
    val kafkaKeystorePath: String,
    val dollyRestUrl: String,
    val azureADConfig: AzureADConfig,
)

class AzureADConfig(
    val discoveryUrl: String,
    val clientId: String,
    val clientSecret: String,
    authorizationUrl: String? = null,
) {
    private val discovered = discoveryUrl.discover()

    val authorizationUrl = authorizationUrl ?: discovered["authorization_endpoint"]?.textValue()
        ?: throw RuntimeException("Unable to discover authorization endpoint")

    val tokenEndpoint = discovered["token_endpoint"]?.textValue()
        ?: throw RuntimeException("Unable to discover token endpoint")
}

private fun String.discover(): JsonNode {
    val (responseCode, responseBody) = this.fetchUrl()
    if (responseCode >= 300 || responseBody == null) throw IOException("got status $responseCode from ${this}.")
    return objectMapper.readTree(responseBody)
}

private fun String.fetchUrl() = with(URL(this).openConnection() as HttpURLConnection) {
    requestMethod = "GET"
    val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
    responseCode to stream?.bufferedReader()?.readText()
}