package no.nav.helse.testdata.dokumenter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Sykmelding(
    val sykmeldingsgrad: Int,
)

fun sykmelding(
    vedtak: Vedtak
): String? = vedtak.sykmelding?.let { sykmelding ->
    """
    {
        "id":"${UUID.randomUUID()}",
        "type":"ARBEIDSTAKERE",
        "fnr":"${vedtak.fnr}",
        "status":"NY",
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
          "sykmeldingsgrad":${sykmelding.sykmeldingsgrad},
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

