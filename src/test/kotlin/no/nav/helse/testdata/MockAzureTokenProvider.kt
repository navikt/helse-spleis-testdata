package no.nav.helse.testdata

import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.ok
import java.time.LocalDateTime

class MockAzureTokenProvider : AzureTokenProvider {
    override fun bearerToken(scope: String) = AzureToken("token", LocalDateTime.MAX).ok()

    override fun onBehalfOfToken(scope: String, token: String): Result<AzureToken> {
        throw NotImplementedError("ikke implementert i mocken")
    }
}