package no.nav.helse.testdata.dokumenter

import no.nav.helse.testdata.assertValidJson
import org.junit.jupiter.api.Test

import java.time.LocalDate

internal class SykmeldingTest {

    companion object {
        val mandag: LocalDate = LocalDate.of(2020, 3, 23)
        val fredag: LocalDate = LocalDate.of(2020, 3, 27)
    }

    @Test
    fun sykmelding() {
        val vedtak = Vedtak(
            fnr = "fnr",
            orgnummer = "orgnummer",
            sykdomFom = mandag,
            sykdomTom = fredag,
            sykmelding = Sykmelding(
                sykmeldingsgrad = 100
            )
        )
        val json = sykmelding(vedtak,"aktørId")
        assertValidJson(json)
    }
}
