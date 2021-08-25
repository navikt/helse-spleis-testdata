package no.nav.helse.testdata.dokumenter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helse.testdata.objectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Inntektsmelding(
    val inntekt: Double,
    val ferieperioder: List<Periode>,
    val arbeidsgiverperiode: List<Periode> = emptyList(),
    val endringRefusjon: List<LocalDate> = emptyList(),
    val opphørRefusjon: LocalDate? = null,
    val førsteFraværsdag: LocalDate? = null,
)

fun inntektsmelding(
    vedtak: Vedtak,
    aktørId: String
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
                "@event_name": "inntektsmelding",
                "@id":"${UUID.randomUUID()}",
                "@opprettet":"${LocalDateTime.now()}",
                "inntektsmeldingId":"${UUID.randomUUID()}",
                "arbeidstakerFnr":"${vedtak.fnr}",
                "arbeidstakerAktorId":"$aktørId",
                "virksomhetsnummer":"${vedtak.orgnummer}",
                "arbeidsgiverFnr":"Don't care",
                "arbeidsgiverAktorId":"Don't care",
                "arbeidsgivertype":"VIRKSOMHET",
                "arbeidsforholdId": "",
                "beregnetInntekt":"${inntektsmelding.inntekt}",
                "rapportertDato":"${vedtak.sykdomFom.plusDays(1)}",
                "refusjon":{
                    "beloepPrMnd":"${inntektsmelding.inntekt}",
                    "opphoersdato":${inntektsmelding.opphørRefusjon?.let { "{\"endringsdato\": \"$it\" }" } ?: "null"}
                },
                "endringIRefusjoner":[${inntektsmelding.endringRefusjon.joinToString { "\"$it\"" }}],
                "opphoerAvNaturalytelser":[],
                "gjenopptakelseNaturalytelser":[],
                "arbeidsgiverperioder": ${arbeidsgiverperioder.tilJson()},
                "ferieperioder": ${inntektsmelding.ferieperioder.tilJson()},
                "status":"GYLDIG",
                "arkivreferanse":"ENARKIVREFERANSE",
                "hendelseId":"${UUID.randomUUID()}",
                "foersteFravaersdag":"$førstefraværsdag",
                "mottattDato":"${vedtak.sykdomFom.atStartOfDay()}"
                } 
        """
    }
}

fun List<Periode>.tilJson(): String = objectMapper.writeValueAsString(this)
