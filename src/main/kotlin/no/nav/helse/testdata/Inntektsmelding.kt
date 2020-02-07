package no.nav.helse.testdata

import java.util.*

fun inntektsmelding(
    vedtak: Vedtak,
    aktørId: String
): String {
    return """
        {
                  "inntektsmeldingId":"${(Math.random() * 10000000).toInt()}",
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
                  "arbeidsgiverperioder":[
                    {
                      "fom":"${vedtak.sykdomFom}",
                      "tom":"${vedtak.sykdomFom.plusDays(15)}"
                    }
                  ],
                  "ferieperioder":[],
                  "status":"GYLDIG",
                  "arkivreferanse":"ENARKIVREFERANSE",
                  "hendelseId":"${UUID.randomUUID()}",
                  "foersteFravaersdag":"${vedtak.sykdomFom}",
                  "mottattDato":"${vedtak.sykdomFom.atStartOfDay()}"
                } 
    """
}

