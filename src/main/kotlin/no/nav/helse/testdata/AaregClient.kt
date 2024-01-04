package no.nav.helse.testdata

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.*
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

class AaregClient(
    private val baseUrl: String,
    private val aaregScope: String,
    private val tokenSupplier: TokenSupplier,
    private val httpClient: HttpClient
) {
    suspend fun hentArbeidsforhold(
        fnr: String,
        callId: UUID
    ): List<AaregArbeidsforhold> {
        val response = hent(
            fnr,
            callId,
            "$baseUrl/v2/arbeidstaker/arbeidsforhold?sporingsinformasjon=false&arbeidsforholdstatus=AKTIV,FREMTIDIG,AVSLUTTET"
        )

        if (response.status.value > 299) throw RuntimeException("feilkode fra aareg: ${response.status}")

        return try {
            sikkerlogg.info("AaregResponse status:\n${response.bodyAsText()}")
            response.body<List<AaregArbeidsforhold>>()
        } catch (e: Exception) {
            val responseValue = objectMapper.readTree(response.bodyAsText())
            throw RuntimeException(responseValue.path("melding").asText("Ukjent respons fra Aareg"))
        }
    }

    private suspend fun hent(fnr: String, callId: UUID, url: String) =
        httpClient.get(url) {
            header("Authorization", "Bearer ${tokenSupplier(aaregScope)}")
            System.getenv("NAIS_APP_NAME")?.also { header("Nav-Consumer-Id", it) }
            header("Nav-Call-Id", callId)
            accept(ContentType.Application.Json)
            header("Nav-Personident", fnr)
        }
}


data class AaregArbeidsforhold(
    val type: Arbeidsforholdkode,
    val arbeidssted: Arbeidssted,
    val ansettelsesperiode: Ansettelsesperiode,
    val ansettelsesdetaljer: List<Ansettelsesdetaljer>,
)

enum class Arbeidsforholdkode(private val kodeHosAAreg: String) {
    FORENKLET_OPPGJØRSORDNING("forenkletOppgjoersordning"),
    FRILANSER("frilanserOppdragstakerHonorarPersonerMm"),
    MARITIMT("maritimtArbeidsforhold"),
    ORDINÆRT("ordinaertArbeidsforhold");

    companion object {
        @JsonCreator
        @JvmStatic
        fun forValues(@JsonProperty("kode") kode: String, @JsonProperty("beskrivelse") beskrivelse: String): Arbeidsforholdkode {
            return values().first { it.kodeHosAAreg == kode }
        }
    }
}

data class Ansettelsesperiode(
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
)

data class Ansettelsesdetaljer(
    val avtaltStillingsprosent: Int,
    val yrke: Yrke,
    val ansettelsesform: Ansettelseform?,
    val rapporteringsmaaneder: Rapporteringsmåneder
)

data class Rapporteringsmåneder(
    val fra: YearMonth,
    val til: YearMonth?
)

data class Yrke(
    val kode: String,
    val beskrivelse: String,
)
data class Ansettelseform(
    val kode: String,
    val beskrivelse: String,
)

data class Arbeidssted(val type: Arbeidsstedtype, val identer: List<Ident>)

enum class Arbeidsstedtype {
    Underenhet,
    Person
}

data class Ident(
    val type: Identtype,
    val ident: String
)

enum class Identtype {
    AKTORID,
    FOLKEREGISTERIDENT,
    ORGANISASJONSNUMMER
}