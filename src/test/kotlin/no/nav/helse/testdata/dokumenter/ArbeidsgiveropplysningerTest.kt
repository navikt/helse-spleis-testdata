package no.nav.helse.testdata.dokumenter

import no.nav.helse.testdata.assertValidJson
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
        val vedtak = Vedtak(
            fnr = "fnr",
            orgnummer = "orgnummer",
            sykdomFom = mandag,
            sykdomTom = fredag,
            arbeidsgiveropplysninger = Arbeidsgiveropplysninger(
                inntekt = 25000.0,
                arbeidsgiverperiode = listOf(Periode(mandag, mandag.plusDays(15))),
                refusjon = Refusjon(),
                begrunnelseForReduksjonEllerIkkeUtbetalt = "",
                vedtaksperiodeId = UUID.randomUUID(),
                forespurt = true,
                arsakTilInnsending = "Ny"
            )
        )
        val json = arbeidsgiveropplysninger(vedtak)
        assertValidJson(json)
    }
}
