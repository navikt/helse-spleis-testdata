package no.nav.helse.testdata

import java.time.LocalDateTime
import java.util.*

fun søknad(
    vedtak: Vedtak,
    aktørId: String
): String? {
    return vedtak.søknad?.let { søknad ->
        """
        {
            "@event_name": "${if (søknad.sendtNav != null) "sendt_søknad_nav" else "sendt_søknad_arbeidsgiver"}",
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
            "sendtNav":${søknad.sendtNav?.atStartOfDay()?.let { "\"$it\"" }},
            "sendtArbeidsgiver":${søknad.sendtArbeidsgiver?.atStartOfDay()?.let { "\"$it\"" }},
            "egenmeldinger":[],
            "papirsykmeldinger":[],
            "fravar":${søknad.ferieperioder.somSøknadsferie()},
            "andreInntektskilder":[${
            if (søknad.harAndreInntektskilder) {
                "{\"type\": \"Arbeid\", \"sykmeldt\": true }"
            } else {
                ""
            }
        }],
            "soknadsperioder":[
            {
              "fom":"${vedtak.sykdomFom}",
              "tom":"${vedtak.sykdomTom}",
              "sykmeldingsgrad":${søknad.sykmeldingsgrad},
              "faktiskGrad":${søknad.faktiskgrad},
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
}

private fun List<Periode>.somSøknadsferie() =
    map {
        """
            {
                "type": "FERIE",
                "fom": "${it.fom}",
                "tom": "${it.tom}"
            }"""
    }

