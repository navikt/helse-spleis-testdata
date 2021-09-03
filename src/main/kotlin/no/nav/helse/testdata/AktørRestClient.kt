package no.nav.helse.testdata

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import java.util.*

internal class AktørRestClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val stsRestClient: StsRestClient
) {
    suspend fun hentAktørId(fødselsnummer: String): Result<String, Exception> =
        httpClient.get<HttpStatement>("$baseUrl/identer") {
            accept(ContentType.Application.Json)
            val oidcToken = stsRestClient.token()
            header("Authorization", "Bearer $oidcToken")
            header("Nav-Consumer-Id", "spleis-testdata")
            header("Nav-Call-Id", UUID.randomUUID().toString())
            header("Nav-Personidenter", fødselsnummer)
            parameter("gjeldende", "true")
            parameter("identgruppe", "AktoerId")
        }.let { response ->
            val result = response.receive<Map<String, IdentInfoResult>>()[fødselsnummer]
            when {
                result == null -> Result.Error(FunctionalFailure("Tomt resultat for ident"))
                result.feilmelding != null -> Result.Error(FunctionalFailure(result.feilmelding))
                result.identer.isNullOrEmpty() -> Result.Error(FunctionalFailure("Fant ikke ident i resultat"))
                else -> Result.Ok(result.identer.first().ident)
            }
        }
}

data class IdentInfo(
    val ident: String,
    val identgruppe: String,
    val gjeldende: Boolean
)

data class IdentInfoResult(
    val identer: List<IdentInfo>?,
    val feilmelding: String?
)
