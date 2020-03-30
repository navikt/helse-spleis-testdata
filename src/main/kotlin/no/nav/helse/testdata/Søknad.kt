package no.nav.helse.testdata

import java.time.LocalDateTime
import java.util.*

fun søknad(
    vedtak: Vedtak,
    aktørId: String
) : String {
    return """
        {
            "@event_name": "sendt_søknad_nav",
            "@id":"${UUID.randomUUID()}",
            "@opprettet":"${LocalDateTime.now()}",
            "id":"${UUID.randomUUID()}",
            "fnr":"${vedtak.fnr}",
            "type":"ARBEIDSTAKERE",
            "status":"SENDT",
            "aktorId":"$aktørId",
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
            "sendtNav":"${vedtak.sendtNav.atStartOfDay()}",
            "sendtArbeidsgiver":"${vedtak.sykdomTom.plusDays(1).atStartOfDay()}",
            "egenmeldinger":[],
            "papirsykmeldinger":null,
            "fravar":[],
            "andreInntektskilder":[${if(vedtak.harAndreInntektskilder) { "{\"type\": \"Arbeid\", \"sykmeldt\": true }"} else {""}}],
            "soknadsperioder":[
            {
              "fom":"${vedtak.sykdomFom}",
              "tom":"${vedtak.sykdomTom}",
              "sykmeldingsgrad":${vedtak.sykmeldingsgrad},
              "faktiskGrad":${vedtak.faktiskgrad},
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

