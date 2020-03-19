package no.nav.helse.testdata

import java.time.LocalDateTime
import java.util.*

fun sykmelding(
    vedtak: Vedtak,
    aktørId: String
) : String {
    return """
        {
            "@event_name": "ny_søknad",
            "@id":"${UUID.randomUUID()}",
            "@opprettet":"${LocalDateTime.now()}",
            "id":"${UUID.randomUUID()}",
            "type":"ARBEIDSTAKERE",
            "fnr":"${vedtak.fnr}",
            "status":"NY",
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
            "sendtNav":null,
            "sendtArbeidsgiver":null,
            "egenmeldinger":[],
            "papirsykmeldinger":null,
            "fravar":[],
            "andreInntektskilder":[],
            "soknadsperioder":[
            {
              "fom":"${vedtak.sykdomFom}",
              "tom":"${vedtak.sykdomTom}",
              "sykmeldingsgrad":${vedtak.sykmeldingsgrad},
              "faktiskGrad":null,
              "avtaltTimer":null,
              "faktiskTimer":null,
              "sykmeldingstype":null
            }
            ],
            "sporsmal":null,
            "hendelseId":"${UUID.randomUUID()}"
        }   
    """.also { assertValidJson(it) }
}

