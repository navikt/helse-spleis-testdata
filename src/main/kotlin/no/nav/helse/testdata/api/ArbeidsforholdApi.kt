package no.nav.helse.testdata.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.testdata.AaregClient
import no.nav.helse.testdata.Arbeidsforholdkode
import no.nav.helse.testdata.Arbeidsstedtype
import no.nav.helse.testdata.Identtype
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

internal fun Routing.registerArbeidsforholdApi(aaregClient: AaregClient) = get("/person/arbeidsforhold") {
    val fnr = requireNotNull(call.request.header("ident")) { "Mangler header: [ident: fnr]" }

    try {
        val response = ArbeidsforholdResponse(
            arbeidsforhold = aaregClient.hentArbeidsforhold(fnr, UUID.randomUUID())
                .map { aaregArbeidsforhold ->
                    ArbeidsforholdDto(
                        type = when (aaregArbeidsforhold.type) {
                            Arbeidsforholdkode.ORDINÆRT -> ArbeidsforholdtypeDto.ORDINÆRT
                            Arbeidsforholdkode.MARITIMT -> ArbeidsforholdtypeDto.MARITIMT
                            Arbeidsforholdkode.FRILANSER -> ArbeidsforholdtypeDto.FRILANSER
                            Arbeidsforholdkode.FORENKLET_OPPGJØRSORDNING -> ArbeidsforholdtypeDto.FORENKLET_OPPGJØRSORDNING
                        },
                        arbeidsgiver = aaregArbeidsforhold.arbeidssted.let { arbeidssted ->
                            ArbeidsgiverDto(
                                type = when (arbeidssted.type) {
                                    Arbeidsstedtype.Underenhet -> ArbeidsgivertypeDto.Organisasjon
                                    Arbeidsstedtype.Person -> ArbeidsgivertypeDto.Person
                                },
                                identifikator = arbeidssted.identer.first {
                                    it.type in setOf(
                                        Identtype.ORGANISASJONSNUMMER,
                                        Identtype.FOLKEREGISTERIDENT
                                    )
                                }.ident,
                            )
                        },
                        ansettelseperiodeFom = aaregArbeidsforhold.ansettelsesperiode.startdato,
                        ansettelseperiodeTom = aaregArbeidsforhold.ansettelsesperiode.sluttdato,
                        detaljer = aaregArbeidsforhold.ansettelsesdetaljer.map { ansettelsesdetaljer ->
                            AnsettelsedetaljeDto(
                                yrke = ansettelsesdetaljer.yrke.beskrivelse,
                                ansettelseform = ansettelsesdetaljer.ansettelsesform?.kode,
                                rapporteringsmaanederFom = ansettelsesdetaljer.rapporteringsmaaneder.fra,
                                rapporteringsmaanederTom = ansettelsesdetaljer.rapporteringsmaaneder.til
                            )
                        }
                    )
                }
        )
        call.respond(response)
    } catch (err: Exception) {
        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(err))
    }
}

class ErrorResponse(err: Exception) {
    val feilmelding = err.message ?: "ukjent feil"
}

data class ArbeidsforholdResponse(
    val arbeidsforhold: List<ArbeidsforholdDto>,
)

data class ArbeidsforholdDto(
    val type: ArbeidsforholdtypeDto,
    val arbeidsgiver: ArbeidsgiverDto,
    val ansettelseperiodeFom: LocalDate,
    val ansettelseperiodeTom: LocalDate?,
    val detaljer: List<AnsettelsedetaljeDto>
)
data class ArbeidsgiverDto(
    val type: ArbeidsgivertypeDto,
    val identifikator: String
)
enum class ArbeidsforholdtypeDto {
    FORENKLET_OPPGJØRSORDNING,
    FRILANSER,
    MARITIMT,
    ORDINÆRT
}
enum class ArbeidsgivertypeDto {
    Organisasjon, Person
}
data class AnsettelsedetaljeDto(
    val yrke: String,
    val ansettelseform: String?,
    val rapporteringsmaanederFom: YearMonth,
    val rapporteringsmaanederTom: YearMonth?
)