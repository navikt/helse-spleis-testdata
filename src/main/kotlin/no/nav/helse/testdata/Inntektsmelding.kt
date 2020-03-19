package no.nav.helse.testdata

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
            "inntektsmeldingId":"${UUID.randomUUID()}",
            "arbeidstakerFnr":"${vedtak.fnr}",
            "arbeidstakerAktorId":"$aktørId",
            "virksomhetsnummer":"${vedtak.orgnummer}",
            "arbeidsgiverFnr":"Don't care",
            "arbeidsgiverAktorId":"Don't care",
            "arbeidsgivertype":"VIRKSOMHET",
            "arbeidsforholdId":"42",
            "beregnetInntekt":"${vedtak.inntekt}",
            "rapportertDato":"${vedtak.sykdomFom.plusDays(1)}",
            "refusjon":{
            "beloepPrMnd":"${vedtak.inntekt}",
            "opphoersdato":null
            },
            "endringIRefusjoner":[],
            "opphoerAvNaturalytelser":[],
            "gjenopptakelseNaturalytelser":[],
            "arbeidsgiverperioder": ${arbeidsgiverperioder.map { """{"fom": "${it.fom}", "tom":"${it.tom}"}""" }},
            "ferieperioder":[],
            "status":"GYLDIG",
            "arkivreferanse":"ENARKIVREFERANSE",
            "hendelseId":"${UUID.randomUUID()}",
            "foersteFravaersdag":"$førstefraværsdag",
            "mottattDato":"${vedtak.sykdomFom.atStartOfDay()}"
            } 
    """
}
