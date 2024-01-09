package no.nav.helse.testdata

import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import java.time.LocalDateTime

class MockAzureTokenProvider : AzureTokenProvider {
    override fun bearerToken(scope: String) = AzureToken("token", LocalDateTime.MAX)

    override fun onBehalfOfToken(scope: String, token: String): AzureToken {
        throw NotImplementedError("ikke implementert i mocken")
    }
}