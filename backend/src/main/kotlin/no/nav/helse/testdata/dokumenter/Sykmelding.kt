package no.nav.helse.testdata.dokumenter

import java.time.LocalDateTime
import java.util.*

data class Sykmelding(
    val sykmeldingsgrad: Int,
)

fun sykmelding(
    vedtak: Vedtak
): String? = vedtak.sykmelding?.let { sykmelding ->
    """
    {
        "id":"${UUID.randomUUID()}",
        "type":"${vedtak.søknad?.arbeidssituasjon?.somSøknadstype()}",
        "fnr":"${vedtak.fnr}",
        "status":"NY",
        "sykmeldingId":"${UUID.randomUUID()}",
        "arbeidsgiver":${vedtak.somArbeidsgiver()},
        "tidligereArbeidsgiverOrgnummer":${vedtak.søknad?.tidligereArbeidsgiverOrgnummer?.somTidligereArbeidsgiverOrgnummer()},
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

