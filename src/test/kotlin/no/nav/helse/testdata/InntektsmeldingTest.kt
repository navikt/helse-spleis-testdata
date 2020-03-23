package no.nav.helse.testdata

import org.junit.jupiter.api.Test

import java.time.LocalDate

internal class InntektsmeldingTest {

    companion object {
        val mandag = LocalDate.of(2020, 3, 23)
        val fredag = LocalDate.of(2020, 3, 27)
    }

    @Test
    fun inntektsmelding() {
        val vedtak = Vedtak(
            fnr = "fnr",
            orgnummer = "orgnummer",
            sykdomFom = mandag,
            sykdomTom = fredag,
            inntekt = 25000.0,
            sendtNav = fredag,
            førstefraværsdag = mandag,
            arbeidsgiverperiode = listOf(Periode(mandag, mandag.plusDays(15))),
            ferieInntektsmelding = listOf(Periode(mandag.plusDays(1), mandag.plusDays(2)))
        )
        val json = inntektsmelding(vedtak,"aktørId")
        assertValidJson(json)
    }
}