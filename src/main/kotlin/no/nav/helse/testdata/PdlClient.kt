package no.nav.helse.testdata

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class PdlClient(
    private val baseUrl: String,
    private val scope: String,
    private val tokenSupplier: TokenSupplier,
    private val httpClient: HttpClient
) {
    private companion object {
        private val hentPersonQuery = "/pdl/hentPerson.graphql".lesFil()

        private fun String.lesFil() =
            PdlClient::class.java.getResource(this)!!
                .readText()
                .replace(Regex("[\n\r\t]"), "")
    }
    suspend fun hentNavn(ident: String, callId: String) = request(ident, callId, hentPersonQuery)

    private suspend fun request(
        ident: String,
        callId: String,
        query: String
    ): JsonNode {
        val aadToken = tokenSupplier(scope)

        val body = PdlQueryObject(query, Variables(ident))

        val response = httpClient.post("$baseUrl/graphql") {
            header("TEMA", "SYK")
            header("Authorization", "Bearer $aadToken")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", callId)
            header("behandlingsnummer", "B139")
            sikkerlogg.info("sender POST $baseUrl/graphql med <$body> til PDL callId=$callId med $aadToken")
            setBody(body)
        }
        val result = objectMapper.readTree(response.bodyAsText())
        if (!response.status.isSuccess())
            throw RuntimeException("error (responseCode=${response.status}) from PDL:\n$result")
        if (result.hasNonNull("errors")) {
            val feilmeldinger = result.path("errors").map { it.path("message").asText() }
            throw RuntimeException("fikk feil fra pdl: ${feilmeldinger.joinToString()}:\n$result")
        }
        return result
    }

    data class PdlQueryObject(
        val query: String,
        val variables: Variables
    )

    data class Variables(
        val ident: String
    )
}