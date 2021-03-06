package no.nav.helse.testdata

import java.time.LocalDateTime
import java.util.*

fun søknad(
    vedtak: Vedtak,
    aktørId: String
) : String {
    return """
        {
            "@event_name": "${if (vedtak.sendtNav != null) "sendt_søknad_nav" else "sendt_søknad_arbeidsgiver"}",
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
            "sendtNav":${vedtak.sendtNav?.atStartOfDay()?.let { "\"$it\"" }},
            "sendtArbeidsgiver":${vedtak.sendtArbeidsgiver?.atStartOfDay()?.let { "\"$it\"" }},
            "egenmeldinger":[],
            "papirsykmeldinger":[],
            "fravar":${vedtak.ferieperioder.somSøknadsferie()},
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

private fun List<Periode>.somSøknadsferie() =
    map { """
            {
                "type": "FERIE",
                "fom": "${it.fom}",
                "tom": "${it.tom}"
            }"""
    }

