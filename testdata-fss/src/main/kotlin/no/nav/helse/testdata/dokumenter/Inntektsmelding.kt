package no.nav.helse.testdata.dokumenter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helse.testdata.dokumenter.EndringIRefusjon.Companion.tilJson
import no.nav.helse.testdata.objectMapper
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Inntektsmelding(
    val inntekt: Double,
    val ferieperioder: List<Periode>,
    val arbeidsgiverperiode: List<Periode> = emptyList(),
    val endringRefusjon: List<EndringIRefusjon> = emptyList(),
    val refusjon: Refusjon,
    val førsteFraværsdag: LocalDate? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Refusjon(
    val refusjonsbeløp: Double? = null,
    val opphørRefusjon: LocalDate? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
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
                "opphoerAvNaturalytelser":[],
                "gjenopptakelseNaturalytelser":[],
                "arbeidsgiverperioder": ${arbeidsgiverperioder.tilJson()},
                "ferieperioder": ${inntektsmelding.ferieperioder.tilJson()},
                "status":"GYLDIG",
                "arkivreferanse":"${UUID.randomUUID()}",
                "hendelseId":"${UUID.randomUUID()}",
                "foersteFravaersdag":"$førstefraværsdag",
                "mottattDato":"${vedtak.sykdomFom.atStartOfDay()}"
                } 
        """
    }
}

fun List<Periode>.tilJson(): String = objectMapper.writeValueAsString(this)
