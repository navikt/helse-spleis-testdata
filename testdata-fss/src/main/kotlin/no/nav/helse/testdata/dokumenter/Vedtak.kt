package no.nav.helse.testdata.dokumenter

import java.time.LocalDate

data class Vedtak(
    val fnr: String,
    val orgnummer: String,
    val sykdomFom: LocalDate,
    val sykdomTom: LocalDate,
    val sykmelding: Sykmelding? = null,
    val søknad: Søknad? = null,
    val inntektsmelding: Inntektsmelding? = null,
    val medlemskapAvklart: Boolean = true
)

data class Periode(val fom: LocalDate, val tom: LocalDate)
