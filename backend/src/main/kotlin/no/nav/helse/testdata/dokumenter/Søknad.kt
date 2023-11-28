package no.nav.helse.testdata.dokumenter

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Søknad(
    val arbeidssituasjon: String,
    val sykmeldingsgrad: Int,
    val harAndreInntektskilder: Boolean,
    val ferieperioder: List<Periode> = emptyList(),
    val egenmeldingsdagerFraSykmelding: List<LocalDate>?,
    val faktiskgrad: Int? = null,
    val sendtNav: LocalDate? = null,
    val sendtArbeidsgiver: LocalDate? = null,
    val arbeidGjenopptatt: LocalDate? = null,
    val tidligereArbeidsgiverOrgnummer: String? = null,
)

fun søknad(
    vedtak: Vedtak
): String? {
    // TODO: hvis orgnummer ikke er satt er hele arbeidsgiver-objektet null?
    return vedtak.søknad?.let { søknad ->
        """
        {
            "id":"${UUID.randomUUID()}",
            "fnr":"${vedtak.fnr}",
            "type":"${vedtak.søknad.arbeidssituasjon.somSøknadstype()}",
            "status":"SENDT",
            "sykmeldingId":"${UUID.randomUUID()}",
            "arbeidsgiver":{
            "navn":"Nærbutikken AS",
            "orgnummer":"${vedtak.orgnummer}"
            },
            "arbeidssituasjon":"${vedtak.søknad.arbeidssituasjon}",
            "tidligereArbeidsgiverOrgnummer":"${vedtak.søknad.tidligereArbeidsgiverOrgnummer}",
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

private fun String.somSøknadstype() = when (this) {
    "FRILANSER" -> "SELVSTENDIGE_OG_FRILANSERE"
    "SELVSTENDIG_NARINGSDRIVENDE" -> "SELVSTENDIGE_OG_FRILANSERE"
    else -> this
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

