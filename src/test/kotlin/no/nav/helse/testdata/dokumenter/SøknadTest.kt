package no.nav.helse.testdata.dokumenter

import no.nav.helse.testdata.assertValidJson
import no.nav.helse.testdata.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
                        belop = 10000,
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
    fun `Søknad uten inntekt`() {
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
                        belop = null,
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
                        belop = 10000,
                        arbeidsstedOrgnummer = "gorgnummer"
                    )
                )
            )
        )
        val json = søknad(vedtak)
        assertValidJson(json)
    }

    @Test
    fun selvstendigSøknadTest() {
        val vedtak = Vedtak(
            fnr = "11111111111",
            orgnummer = null,
            sykdomFom = mandag,
            sykdomTom = fredag,
            arbeidssituasjon = "SELVSTENDIG_NARINGSDRIVENDE",
            søknad = Søknad(
                sykmeldingsgrad = 100,
                harAndreInntektskilder = false,
                sendtNav = fredag,
                ferieperioder = listOf(
                    Periode(mandag.plusDays(1), mandag.plusDays(2)),
                    Periode(mandag.plusDays(100), mandag.plusDays(101))
                ),
                egenmeldingsdagerFraSykmelding = listOf(torsdag),
                inntektFraNyttArbeidsforhold = emptyList(),
                inntektFraSigrun = 600000
            )
        )
        val json = søknad(vedtak)

        assertValidJson(json)
    }

    @Test
    fun `selvstendig søknad med meldingTilNavDagerFraSykmelding inneholder periode i JSON`() {
        val fom = mandag
        val tom = fredag
        val vedtak = Vedtak(
            fnr = "11111111111",
            orgnummer = null,
            sykdomFom = mandag,
            sykdomTom = fredag,
            arbeidssituasjon = "SELVSTENDIG_NARINGSDRIVENDE",
            søknad = Søknad(
                sykmeldingsgrad = 100,
                harAndreInntektskilder = false,
                sendtNav = fredag,
                ferieperioder = emptyList(),
                egenmeldingsdagerFraSykmelding = emptyList(),
                inntektFraNyttArbeidsforhold = emptyList(),
                inntektFraSigrun = 600000,
                meldingTilNavDagerFraSykmelding = Periode(fom, tom)
            )
        )
        val json = søknad(vedtak)!!

        assertValidJson(json)
        val tree = objectMapper.readTree(json)
        val meldingTilNav = tree.path("meldingTilNavDagerFraSykmelding")
        assertFalse(meldingTilNav.isNull, "meldingTilNavDagerFraSykmelding skal ikke være null")
        assertEquals(1, meldingTilNav.size())
        assertEquals("$fom", meldingTilNav[0].path("fom").asText())
        assertEquals("$tom", meldingTilNav[0].path("tom").asText())
    }

    @Test
    fun `søknad uten meldingTilNavDagerFraSykmelding er null i JSON`() {
        val vedtak = Vedtak(
            fnr = "11111111111",
            orgnummer = null,
            sykdomFom = mandag,
            sykdomTom = fredag,
            arbeidssituasjon = "SELVSTENDIG_NARINGSDRIVENDE",
            søknad = Søknad(
                sykmeldingsgrad = 100,
                harAndreInntektskilder = false,
                sendtNav = fredag,
                ferieperioder = emptyList(),
                egenmeldingsdagerFraSykmelding = emptyList(),
                inntektFraNyttArbeidsforhold = emptyList(),
                inntektFraSigrun = 600000,
                meldingTilNavDagerFraSykmelding = null
            )
        )
        val json = søknad(vedtak)!!

        assertValidJson(json)
        val tree = objectMapper.readTree(json)
        assertTrue(tree.path("meldingTilNavDagerFraSykmelding").isNull,
            "meldingTilNavDagerFraSykmelding skal være null når den ikke er satt")
    }

    @Test
    fun `meldingTilNavDagerFraSykmelding er null i JSON for ARBEIDSTAKER`() {
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
                ferieperioder = emptyList(),
                egenmeldingsdagerFraSykmelding = emptyList(),
                inntektFraNyttArbeidsforhold = emptyList(),
                meldingTilNavDagerFraSykmelding = Periode(mandag, fredag)
            )
        )
        val json = søknad(vedtak)!!

        assertValidJson(json)
        val tree = objectMapper.readTree(json)
        assertTrue(tree.path("meldingTilNavDagerFraSykmelding").isNull,
            "meldingTilNavDagerFraSykmelding skal være null for ARBEIDSTAKER selv om verdien er satt")
    }
}
