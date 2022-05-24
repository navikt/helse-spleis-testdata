package no.nav.helse.testdata

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.client.*
import io.ktor.server.auth.jwt.*
import java.net.URL

internal fun setUpEnvironment() =
    Environment(
        kafkaBrokers = System.getenv("KAFKA_BROKERS") ?: error("Mangler env var KAFKA_BROKERS"),
        kafkaCredstorePassword = System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        kafkaTruststorePath = System.getenv("KAFKA_TRUSTSTORE_PATH"),
        kafkaKeystorePath = System.getenv("KAFKA_KEYSTORE_PATH"),
        dollyRestUrl = "https://dolly-backend.dev.intern.nav.no/api/v1",
        azureADConfig = AzureADConfig(
            clientId = System.getenv("AZURE_APP_CLIENT_ID"),
            clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET"),
            aadAccessTokenUrl = System.getenv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            issuer = System.getenv("AZURE_OPENID_CONFIG_ISSUER"),
            jwkProvider = JwkProviderBuilder(URL(System.getenv("AZURE_OPENID_CONFIG_JWKS_URI"))).build()
        )
    )

internal data class Environment(
    val kafkaBrokers: String,
    val kafkaCredstorePassword: String,
    val kafkaTruststorePath: String,
    val kafkaKeystorePath: String,
    val dollyRestUrl: String,
    val azureADConfig: AzureADConfig,
)

internal class AzureADConfig(
    private val clientId: String,
    private val issuer: String,
    private val jwkProvider: JwkProvider,
    private val clientSecret: String,
    private val aadAccessTokenUrl: String,
) {
    fun configureAuthentication(configuration: JWTAuthenticationProvider.Config) {
        configuration.verifier(jwkProvider, issuer) {
            withAudience(clientId)
        }
        configuration.validate { credentials -> JWTPrincipal(credentials.payload) }
    }

    fun accessTokenClient(httpClient: HttpClient): AccessTokenClient {
        return AccessTokenClient(
            aadAccessTokenUrl = aadAccessTokenUrl,
            clientId = clientId,
            clientSecret = clientSecret,
            httpClient = httpClient,
        )
    }
}