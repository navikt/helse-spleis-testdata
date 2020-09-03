package no.nav.helse.testdata

import java.time.LocalDateTime
import java.util.*

fun inntektsmelding(
    vedtak: Vedtak,
    aktørId: String
): String {
    val førstefraværsdag = vedtak.førstefraværsdag ?: vedtak.sykdomFom
    val arbeidsgiverperioder = vedtak.arbeidsgiverperiode.takeIf { it.isNotEmpty() } ?: listOf(Periode(førstefraværsdag, førstefraværsdag.plusDays(15)))

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
            "beregnetInntekt":"${vedtak.inntekt}",
            "rapportertDato":"${vedtak.sykdomFom.plusDays(1)}",
            "refusjon":{
                "beloepPrMnd":"${vedtak.inntekt}",
                "opphoersdato":${vedtak.opphørRefusjon?.let { "{\"endringsdato\": \"$it\" }" } ?: "null"}
            },
            "endringIRefusjoner":[${vedtak.endringRefusjon.joinToString { "\"$it\"" }}],
            "opphoerAvNaturalytelser":[],
            "gjenopptakelseNaturalytelser":[],
            "arbeidsgiverperioder": ${arbeidsgiverperioder.tilJson()},
            "ferieperioder": ${vedtak.ferieperioder.tilJson()},
            "status":"GYLDIG",
            "arkivreferanse":"ENARKIVREFERANSE",
            "hendelseId":"${UUID.randomUUID()}",
            "foersteFravaersdag":"$førstefraværsdag",
            "mottattDato":"${vedtak.sykdomFom.atStartOfDay()}"
            } 
    """
}

fun List<Periode>.tilJson(): String = objectMapper.writeValueAsString(this)
