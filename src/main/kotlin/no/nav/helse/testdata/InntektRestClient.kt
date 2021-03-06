package no.nav.helse.testdata

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import java.time.YearMonth

internal class InntektRestClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val stsRestClient: StsRestClient
) {
    suspend fun hentInntektsliste(
        fnr: String,
        fom: YearMonth,
        tom: YearMonth,
        filter: String,
        callId: String
    ): Result<List<Måned>, ResponseFailure> =
        httpClient.request<HttpStatement>("$baseUrl/api/v1/hentinntektliste") {
            method = HttpMethod.Post
            header("Authorization", "Bearer ${stsRestClient.token()}")
            header("Nav-Consumer-Id", "srvspleistestdata")
            header("Nav-Call-Id", callId)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            body = mapOf(
                "ident" to mapOf(
                    "identifikator" to fnr,
                    "aktoerType" to "NATURLIG_IDENT"
                ),
                "ainntektsfilter" to filter,
                // TODO: Bruker Foreldrepenger midlertidig på grunn av mangel på tilgang til 8-28 og 8-30 som Sykepenger
                "formaal" to "Foreldrepenger",
                "maanedFom" to fom,
                "maanedTom" to tom
            )
        }.let {
            Result.Ok(toMånedListe(objectMapper.readValue(it.receive<String>())))
            }
}

private fun toMånedListe(node: JsonNode) = node.path("arbeidsInntektMaaned").map(::tilMåned)

private fun toInntekt(node: JsonNode) = Inntekt(
    beløp = node["beloep"].asDouble(),
    inntektstype = Inntektstype.valueOf(node["inntektType"].textValue()),
    orgnummer = node["virksomhet"].let {
        if (it["aktoerType"].asText() == "ORGANISASJON") {
            it["identifikator"].asText()
        } else {
            null
        }
    }
)

private fun tilMåned(node: JsonNode) = Måned(
    YearMonth.parse(node["aarMaaned"].asText()),
    node["arbeidsInntektInformasjon"]["inntektListe"].map(::toInntekt)
)

data class Måned(
    val årMåned: YearMonth,
    val inntektsliste: List<Inntekt>
)

data class Inntekt(
    val beløp: Double,
    val inntektstype: Inntektstype,
    val orgnummer: String?
)

enum class Inntektstype {
    LOENNSINNTEKT,
    NAERINGSINNTEKT,
    PENSJON_ELLER_TRYGD,
    YTELSE_FRA_OFFENTLIGE
}
