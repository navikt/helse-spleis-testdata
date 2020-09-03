package no.nav.helse.testdata

import org.junit.jupiter.api.Test

import java.time.LocalDate

internal class SykmeldingTest {

    companion object {
        val mandag = LocalDate.of(2020, 3, 23)
        val fredag = LocalDate.of(2020, 3, 27)
    }

    @Test
    fun sykmelding() {
        val vedtak = Vedtak(
            fnr = "fnr",
            orgnummer = "orgnummer",
            sykdomFom = mandag,
            sykdomTom = fredag,
            inntekt = 25000.0,
            sendtNav = fredag,
            førstefraværsdag = mandag,
            arbeidsgiverperiode = emptyList(),
            ferieperioder = emptyList()
        )
        val json = sykmelding(vedtak,"aktørId")
        assertValidJson(json)
    }
}
