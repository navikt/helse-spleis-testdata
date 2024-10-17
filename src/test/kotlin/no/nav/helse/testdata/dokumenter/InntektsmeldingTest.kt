package no.nav.helse.testdata.dokumenter

import no.nav.helse.testdata.assertValidJson
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class InntektsmeldingTest {

    companion object {
        val mandag: LocalDate = LocalDate.of(2020, 3, 23)
        val fredag: LocalDate = LocalDate.of(2020, 3, 27)
    }

    @Test
    fun inntektsmelding() {
        val vedtak = Vedtak(
            fnr = "fnr",
            orgnummer = "orgnummer",
            sykdomFom = mandag,
            sykdomTom = fredag,
            arbeidssituasjon = "ARBEIDSTAKERE",
            inntektsmelding = Inntektsmelding(
                inntekt = 25000.0,
                førsteFraværsdag = mandag,
                arbeidsgiverperiode = listOf(Periode(mandag, mandag.plusDays(15))),
                refusjon = Refusjon(),
                begrunnelseForReduksjonEllerIkkeUtbetalt = ""
            )
        )
        val json = inntektsmelding(vedtak)
        assertValidJson(json)
    }
}
