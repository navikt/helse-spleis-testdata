package no.nav.helse.testdata.dokumenter

import no.nav.helse.testdata.assertValidJson
import no.nav.helse.testdata.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class ArbeidsgiveropplysningerTest {
    companion object {
        val mandag: LocalDate = LocalDate.of(2020, 3, 23)
        val fredag: LocalDate = LocalDate.of(2020, 3, 27)
    }

    @Test
    fun arbeidsgiveropplysninger() {
        val json = arbeidsgiveropplysninger(vedtak(Refusjon()))
        assertValidJson(json)
    }

    @Test
    fun `refusjonsbeløp som mangler sendes som null`() {
        val json = objectMapper.readTree(arbeidsgiveropplysninger(vedtak(Refusjon())))
        assertTrue(json.path("refusjon").path("beloepPrMnd").isNull)
    }

    @Test
    fun `refusjonsbeløp sendes som tekst`() {
        val json = objectMapper.readTree(arbeidsgiveropplysninger(vedtak(Refusjon(refusjonsbeløp = 25000.0))))
        assertEquals("25000.0", json.path("refusjon").path("beloepPrMnd").asText())
    }

    private fun vedtak(refusjon: Refusjon) =
        Vedtak(
            fnr = "fnr",
            orgnummer = "orgnummer",
            sykdomFom = mandag,
            sykdomTom = fredag,
            arbeidsgiveropplysninger =
                Arbeidsgiveropplysninger(
                    inntekt = 25000.0,
                    arbeidsgiverperiode = listOf(Periode(mandag, mandag.plusDays(15))),
                    refusjon = refusjon,
                    begrunnelseForReduksjonEllerIkkeUtbetalt = "",
                    vedtaksperiodeId = UUID.randomUUID(),
                    forespurt = true,
                    arsakTilInnsending = "Ny",
                ),
        )
}
