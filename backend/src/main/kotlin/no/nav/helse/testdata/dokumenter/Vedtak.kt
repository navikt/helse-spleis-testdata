package no.nav.helse.testdata.dokumenter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class Vedtak(
    val fnr: String,
    val orgnummer: String?,
    val sykdomFom: LocalDate,
    val sykdomTom: LocalDate,
    val sykmelding: Sykmelding? = null,
    val søknad: Søknad? = null,
    val inntektsmelding: Inntektsmelding? = null,
    val medlemskapAvklart: Boolean = true
)

data class Periode(val fom: LocalDate, val tom: LocalDate)

internal fun Vedtak.somArbeidsgiver(): String? {
    val yrkesAktiviteterUtenArbeidsgiver = listOf("ARBEIDSLEDIG", "FRILANSER", "SELVSTENDIG_NARINGSDRIVENDE")
    if (yrkesAktiviteterUtenArbeidsgiver.contains(søknad?.arbeidssituasjon)) {
        return null
    } else {
        return "{\"navn\": \"Nærbutikken AS\", \"orgnummer\": \"${orgnummer}\" }"
    }
}

internal fun String?.somTidligereArbeidsgiverOrgnummer(): String? {
    if (this !== null) {
        return "\"$this\""
    }
    return null
}
internal fun String?.somSøknadstype() = when (this) {
    null -> "ARBEIDSTAKERE"
    "FRILANSER" -> "SELVSTENDIGE_OG_FRILANSERE"
    "SELVSTENDIG_NARINGSDRIVENDE" -> "SELVSTENDIGE_OG_FRILANSERE"
    "ARBEIDSTAKER" -> "ARBEIDSTAKERE"
    else -> this
}