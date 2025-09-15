package no.nav.helse.testdata.dokumenter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class Vedtak(
    val fnr: String,
    val orgnummer: String?,
    val sykdomFom: LocalDate,
    val sykdomTom: LocalDate,
    val arbeidssituasjon: String? = null,
    val sykmelding: Sykmelding? = null,
    val søknad: Søknad? = null,
    val inntektsmelding: Inntektsmelding? = null,
    val medlemskapVerdi: String = "JA"
)

data class Periode(val fom: LocalDate, val tom: LocalDate)

internal fun Vedtak.somArbeidsgiver(): String? {
    val yrkesAktiviteterUtenArbeidsgiver = listOf("ARBEIDSLEDIG", "FRILANSER", "SELVSTENDIG_NARINGSDRIVENDE", "BARNEPASSER")
    if (yrkesAktiviteterUtenArbeidsgiver.contains(arbeidssituasjon)) {
        return null
    } else {
        val orgnummer = if (orgnummer != null) "\"$orgnummer\"" else null
        return """{"navn": "Nærbutikken AS", "orgnummer": $orgnummer }"""
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
    "FRILANSER",
    "SELVSTENDIG_NARINGSDRIVENDE",
    "BARNEPASSER" -> "SELVSTENDIGE_OG_FRILANSERE"
    "ARBEIDSTAKER" -> "ARBEIDSTAKERE"
    else -> this
}
