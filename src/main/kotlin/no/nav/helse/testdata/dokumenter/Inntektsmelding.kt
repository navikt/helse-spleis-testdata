package no.nav.helse.testdata.dokumenter

import no.nav.helse.testdata.dokumenter.EndringIRefusjon.Companion.tilJson
import no.nav.helse.testdata.objectMapper
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Inntektsmelding(
    val inntekt: Double,
    val arbeidsgiverperiode: List<Periode> = emptyList(),
    val endringRefusjon: List<EndringIRefusjon> = emptyList(),
    val refusjon: Refusjon,
    val førsteFraværsdag: LocalDate? = null,
    val begrunnelseForReduksjonEllerIkkeUtbetalt: String = "",
    val harOpphørAvNaturalytelser: Boolean = false
)

data class Refusjon(
    val refusjonsbeløp: Double? = null,
    val opphørRefusjon: LocalDate? = null,
)

data class EndringIRefusjon(
    val endringsdato: LocalDate,
    val beløp: Double
) {
    @Language("JSON")
    internal fun tilJson() = """{"endringsdato":"$endringsdato", "beloep": "$beløp"}"""
    internal companion object {
        internal fun List<EndringIRefusjon>.tilJson() = joinToString(",", prefix = "[", postfix = "]", transform = EndringIRefusjon::tilJson)
    }
}

fun inntektsmelding(
    vedtak: Vedtak
): String? {
    return vedtak.inntektsmelding?.let { inntektsmelding ->
        val førstefraværsdag = inntektsmelding.førsteFraværsdag ?: vedtak.sykdomFom
        val arbeidsgiverperioder = inntektsmelding.arbeidsgiverperiode.takeIf { it.isNotEmpty() } ?: listOf(
            Periode(
                førstefraværsdag,
                førstefraværsdag.plusDays(15)
            )
        )

        return """
            {
                "inntektsmeldingId":"${UUID.randomUUID()}",
                "arbeidstakerFnr":"${vedtak.fnr}",
                "virksomhetsnummer":"${vedtak.orgnummer}",
                "arbeidsgiverFnr":"Don't care",
                "arbeidsgiverAktorId":"Don't care",
                "arbeidsgivertype":"VIRKSOMHET",
                "arbeidsforholdId": "",
                "beregnetInntekt":"${inntektsmelding.inntekt}",
                "rapportertDato":"${vedtak.sykdomFom.plusDays(1)}",
                "refusjon":{
                    "beloepPrMnd":"${inntektsmelding.refusjon.refusjonsbeløp}",
                    "opphoersdato": ${inntektsmelding.refusjon.opphørRefusjon?.let { "\"$it\"" }}
                },
                "endringIRefusjoner": ${inntektsmelding.endringRefusjon.tilJson()},
                "opphoerAvNaturalytelser": ${ if (inntektsmelding.harOpphørAvNaturalytelser) """[{"naturalytelse":"ANNET", "beloepPrMnd":"1200.0", "fom":"${inntektsmelding.førsteFraværsdag ?: "2018-01-01"}"}]""" else "[]"},
                "begrunnelseForReduksjonEllerIkkeUtbetalt": "${inntektsmelding.begrunnelseForReduksjonEllerIkkeUtbetalt}",
                "gjenopptakelseNaturalytelser":[],
                "arbeidsgiverperioder": ${arbeidsgiverperioder.tilJson()},
                "ferieperioder": [],
                "status":"GYLDIG",
                "arkivreferanse":"${UUID.randomUUID()}",
                "hendelseId":"${UUID.randomUUID()}",
                "foersteFravaersdag":"$førstefraværsdag",
                "testdataOpprettet":"${LocalDateTime.now()}",
                "mottattDato":"${vedtak.sykdomFom.atStartOfDay()}",
                "innsenderFulltNavn": "spleis-testdata",
                "innsenderTelefon": "123456789",
                "matcherSpleis":true
            } 
        """
    }
}

fun List<Periode>.tilJson(): String = objectMapper.writeValueAsString(this)
