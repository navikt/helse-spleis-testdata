package no.nav.helse.testdata.dokumenter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Søknad(
    val sykmeldingsgrad: Int,
    val harAndreInntektskilder: Boolean,
    val ferieperioder: List<Periode> = emptyList(),
    val faktiskgrad: Int? = null,
    val sendtNav: LocalDate? = null,
    val sendtArbeidsgiver: LocalDate? = null,
)

fun søknad(
    vedtak: Vedtak
): String? {
    return vedtak.søknad?.let { søknad ->
        """
        {
            "id":"${UUID.randomUUID()}",
            "fnr":"${vedtak.fnr}",
            "type":"ARBEIDSTAKERE",
            "status":"SENDT",
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
            "testdataOpprettet":"${LocalDateTime.now()}",
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

