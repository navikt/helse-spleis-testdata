package no.nav.helse.testdata.dokumenter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class Vedtak(
    val fnr: String,
    val orgnummer: String,
    val sykdomFom: LocalDate,
    val sykdomTom: LocalDate,
    val sykmelding: Sykmelding? = null,
    val søknad: Søknad? = null,
    val inntektsmelding: Inntektsmelding? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Periode(val fom: LocalDate, val tom: LocalDate)
