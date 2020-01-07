package no.nav.helse.testdata

import java.util.*

fun søknad(
    vedtak: Vedtak
) : String {
    return """
        {
                      "id":${UUID.randomUUID()}",
                      "type":"ARBEIDSTAKERE",
                      "status":"SENDT",
                      "aktorId":"${vedtak.aktørId}",
                      "sykmeldingId":"${UUID.randomUUID()}",
                      "arbeidsgiver":{
                        "navn":"Nærbutikken AS",
                        "orgnummer":"${vedtak.orgnummer}"
                      },
                      "arbeidssituasjon":"ARBEIDSTAKER",
                      "korrigerer":null,
                      "korrigertAv":null,
                      "soktUtenlandsopphold":null,
                      "arbeidsgiverForskutterer":null,
                      "fom":"${vedtak.sykdomFom}",
                      "tom":"${vedtak.sykdomTom}",
                      "startSyketilfelle":"${vedtak.sykdomFom}",
                      "arbeidGjenopptatt":null,
                      "sykmeldingSkrevet":"${vedtak.sykdomFom.atStartOfDay()}",
                      "opprettet":"${vedtak.sykdomFom.atStartOfDay()}",
                      "sendtNav":"${vedtak.sykdomTom.plusDays(1).atStartOfDay()}",
                      "sendtArbeidsgiver":"${vedtak.sykdomTom.plusDays(1).atStartOfDay()}",
                      "egenmeldinger":[],
                      "papirsykmeldinger":null,
                      "fravar":[],
                      "andreInntektskilder":[],
                      "soknadsperioder":[
                        {
                          "fom":"${vedtak.sykdomFom}",
                          "tom":"${vedtak.sykdomTom}",
                          "sykmeldingsgrad":100,
                          "faktiskGrad":null,
                          "avtaltTimer":null,
                          "faktiskTimer":null,
                          "sykmeldingstype":null
                        }
                      ],
                      "sporsmal":null,
                      "hendelseId":"${UUID.randomUUID()}"
                    }   
    """
}

