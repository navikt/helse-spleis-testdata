package no.nav.helse.testdata.dokumenter

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Søknad(
    val sykmeldingsgrad: Int,
    val harAndreInntektskilder: Boolean,
    val ferieperioder: List<Periode> = emptyList(),
    val egenmeldingsdagerFraSykmelding: List<LocalDate>?,
    val inntektFraNyttArbeidsforhold: List<InntektFraNyttArbeidsforholdDto>?,
    val faktiskgrad: Int? = null,
    val sendtNav: LocalDate? = null,
    val sendtArbeidsgiver: LocalDate? = null,
    val arbeidGjenopptatt: LocalDate? = null,
    val tidligereArbeidsgiverOrgnummer: String? = null,
) {
    data class InntektFraNyttArbeidsforholdDto(
        val datoFom: LocalDate,
        val datoTom: LocalDate,
        val belopPerDag: Int,
        val arbeidsstedOrgnummer: String
    )
}

fun søknad(
    vedtak: Vedtak
): String? {
    return vedtak.søknad?.let { søknad ->
        """
        {
            "id":"${UUID.randomUUID()}",
            "fnr":"${vedtak.fnr}",
            "type":"${vedtak.arbeidssituasjon.somSøknadstype()}",
            "status":"SENDT",
            "sykmeldingId":"${UUID.randomUUID()}",
            "arbeidsgiver": ${vedtak.somArbeidsgiver()},
            "arbeidssituasjon":"${vedtak.arbeidssituasjon}",
            "tidligereArbeidsgiverOrgnummer":${vedtak.søknad.tidligereArbeidsgiverOrgnummer?.somTidligereArbeidsgiverOrgnummer()},
            "korrigerer":null,
            "korrigertAv":null,
            "soktUtenlandsopphold":null,
            "arbeidsgiverForskutterer":null,
            "fom":"${vedtak.sykdomFom}",
            "tom":"${vedtak.sykdomTom}",
            "startSyketilfelle":"${vedtak.sykdomFom}",
            "arbeidGjenopptatt":${søknad.arbeidGjenopptatt?.let { "\"$it\"" }},
            "sykmeldingSkrevet":"${vedtak.sykdomFom.atStartOfDay()}",
            "opprettet":"${vedtak.sykdomFom.atStartOfDay()}",
            "testdataOpprettet":"${LocalDateTime.now()}",
            "sendtNav":${søknad.sendtNav?.atStartOfDay()?.let { "\"$it\"" }},
            "sendtArbeidsgiver":${søknad.sendtArbeidsgiver?.atStartOfDay()?.let { "\"$it\"" }},
            "egenmeldinger":[],
            "egenmeldingsdagerFraSykmelding": ${søknad.egenmeldingsdagerFraSykmelding?.map { "\"$it\"" }},
            "inntektFraNyttArbeidsforhold": ${søknad.inntektFraNyttArbeidsforhold?.somInntektFraNyttArbeidsforhold()},
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

private fun List<Søknad.InntektFraNyttArbeidsforholdDto>.somInntektFraNyttArbeidsforhold() =
    map {
        """
            {
                "arbeidsstedOrgnummer": "${it.arbeidsstedOrgnummer}",
                "fom": "${it.datoFom}",
                "tom": "${it.datoTom}",
                "belopPerDag": "${it.belopPerDag}"
            }"""
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


