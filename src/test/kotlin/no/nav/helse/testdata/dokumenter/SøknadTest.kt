package no.nav.helse.testdata.dokumenter

import no.nav.helse.testdata.assertValidJson
import org.junit.jupiter.api.Test

import java.time.LocalDate

internal class SøknadTest {

    companion object {
        val torsdag: LocalDate = LocalDate.of(2020, 3, 19)
        val mandag: LocalDate = LocalDate.of(2020, 3, 23)
        val fredag: LocalDate = LocalDate.of(2020, 3, 27)
    }

    @Test
    fun søknad() {
        val vedtak = Vedtak(
            fnr = "fnr",
            orgnummer = "orgnummer",
            sykdomFom = mandag,
            sykdomTom = fredag,
            arbeidssituasjon = "ARBEIDSTAKER",
            søknad = Søknad(
                sykmeldingsgrad = 100,
                harAndreInntektskilder = false,
                sendtNav = fredag,
                ferieperioder = listOf(
                    Periode(mandag.plusDays(1), mandag.plusDays(2)),
                    Periode(mandag.plusDays(100), mandag.plusDays(101))
                ),
                inntektFraNyttArbeidsforhold = listOf(
                    Søknad.InntektFraNyttArbeidsforholdDto(
                        datoFom = mandag,
                        datoTom = fredag,
                        belopPerDag = 10000,
                        arbeidsstedOrgnummer = "gorgnummer"
                    )
                ),
                egenmeldingsdagerFraSykmelding = listOf(torsdag),
            )
        )
        val json = søknad(vedtak)
        assertValidJson(json)
    }

    @Test
    fun arbeidsledigSøknad() {
        val vedtak = Vedtak(
            fnr = "fnr",
            orgnummer = null,
            sykdomFom = mandag,
            sykdomTom = fredag,
            arbeidssituasjon = "ARBEIDSLEDIG",
            søknad = Søknad(
                sykmeldingsgrad = 100,
                harAndreInntektskilder = false,
                sendtNav = fredag,
                ferieperioder = listOf(
                    Periode(mandag.plusDays(1), mandag.plusDays(2)),
                    Periode(mandag.plusDays(100), mandag.plusDays(101))
                ),
                egenmeldingsdagerFraSykmelding = listOf(torsdag),
                tidligereArbeidsgiverOrgnummer = "orgnummer",
                inntektFraNyttArbeidsforhold = listOf(
                    Søknad.InntektFraNyttArbeidsforholdDto(
                        datoFom = mandag,
                        datoTom = fredag,
                        belopPerDag = 10000,
                        arbeidsstedOrgnummer = "gorgnummer"
                    )
                )
            )
        )
        val json = søknad(vedtak)
        assertValidJson(json)
    }
}
