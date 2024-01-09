package no.nav.helse.testdata

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.time.YearMonth

internal class InntektRestClient(
    private val baseUrl: String,
    private val inntektClientId: String,
    private val tokenSupplier: AzureTokenProvider,
    private val httpClient: HttpClient,
) {
    suspend fun hentInntektsliste(
        fnr: String,
        fom: YearMonth,
        tom: YearMonth,
        filter: String,
        callId: String
    ): Result<List<Måned>, ResponseFailure> =
        httpClient.post("$baseUrl/api/v1/hentinntektliste") {
            bearerAuth(tokenSupplier.bearerToken(inntektClientId).token)
            header("Nav-Consumer-Id", "spleis-testdata")
            header("Nav-Call-Id", callId)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                mapOf(
                    "ident" to mapOf(
                        "identifikator" to fnr,
                        "aktoerType" to "NATURLIG_IDENT"
                    ),
                    "ainntektsfilter" to filter,
                    "formaal" to "Sykepenger",
                    "maanedFom" to fom,
                    "maanedTom" to tom,
                )
            )
        }.let {
            Result.Ok(toMånedListe(objectMapper.readValue(it.body<String>())))
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
