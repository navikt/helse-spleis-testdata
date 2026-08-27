package no.nav.helse.testdata.dokumenter

import no.nav.helse.testdata.dokumenter.EndringIRefusjon.Companion.tilJson
import no.nav.helse.testdata.objectMapper
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Arbeidsgiveropplysninger(
    val inntekt: Double,
    val arbeidsgiverperiode: List<Periode> = emptyList(),
    val endringRefusjon: List<EndringIRefusjon> = emptyList(),
    val refusjon: Refusjon,
    val begrunnelseForReduksjonEllerIkkeUtbetalt: String = "",
    val harOpphørAvNaturalytelser: Boolean = false,
    val vedtaksperiodeId: UUID,
    val forespurt: Boolean,
    val arsakTilInnsending: String? = null
)

data class Refusjon(
    val refusjonsbeløp: Double? = null,
    val opphørRefusjon: LocalDate? = null,
)

data class EndringIRefusjon(
    val endringsdato: LocalDate,
    val beløp: Double
) {
    @Language("JSON")
    internal fun tilJson() = """{"endringsdato":"$endringsdato", "beloep": "$beløp"}"""
    internal companion object {
        internal fun List<EndringIRefusjon>.tilJson() = joinToString(",", prefix = "[", postfix = "]", transform = EndringIRefusjon::tilJson)
    }
}

fun arbeidsgiveropplysninger(
    vedtak: Vedtak
): String? {
    return vedtak.arbeidsgiveropplysninger?.let { arbeidsgiveropplysninger ->
        val arbeidsgiverperioder = arbeidsgiveropplysninger.arbeidsgiverperiode
        val opphoerAvNaturalytelserFom = arbeidsgiverperioder.minOfOrNull { it.fom }?.toString() ?: "2018-01-01"
        @Language("JSON")
        return """
            {
                "inntektsmeldingId":"${UUID.randomUUID()}",
                "arbeidstakerFnr":"${vedtak.fnr}",
                "virksomhetsnummer":"${vedtak.orgnummer}",
                "arbeidsgiverFnr":"Don't care",
                "arbeidsgiverAktorId":"Don't care",
                "arbeidsgivertype":"VIRKSOMHET",
                "arbeidsforholdId": "",
                "beregnetInntekt":"${arbeidsgiveropplysninger.inntekt}",
                "rapportertDato":"${vedtak.sykdomFom.plusDays(1)}",
                "refusjon":{
                    "beloepPrMnd":"${arbeidsgiveropplysninger.refusjon.refusjonsbeløp}",
                    "opphoersdato": ${arbeidsgiveropplysninger.refusjon.opphørRefusjon?.let { "\"$it\"" }}
                },
                "endringIRefusjoner": ${arbeidsgiveropplysninger.endringRefusjon.tilJson()},
                "opphoerAvNaturalytelser": ${ if (arbeidsgiveropplysninger.harOpphørAvNaturalytelser) """[{"naturalytelse":"ANNET", "beloepPrMnd":"1200.0", "fom":"$opphoerAvNaturalytelserFom"}]""" else "[]"},
                "begrunnelseForReduksjonEllerIkkeUtbetalt": "${arbeidsgiveropplysninger.begrunnelseForReduksjonEllerIkkeUtbetalt}",
                "gjenopptakelseNaturalytelser":[],
                "arbeidsgiverperioder": ${arbeidsgiverperioder.tilJson()},
                "ferieperioder": [],
                "status":"GYLDIG",
                "arkivreferanse":"${UUID.randomUUID()}",
                "hendelseId":"${UUID.randomUUID()}",
                "testdataOpprettet":"${LocalDateTime.now()}",
                "mottattDato":"${vedtak.sykdomFom.atStartOfDay()}",
                "innsenderFulltNavn": "spleis-testdata",
                "innsenderTelefon": "123456789",
                "format":"Arbeidsgiveropplysninger",
                "vedtaksperiodeId": "${vedtak.arbeidsgiveropplysninger.vedtaksperiodeId}",
                "forespurt": ${vedtak.arbeidsgiveropplysninger.forespurt}
                ${if (vedtak.arbeidsgiveropplysninger.arsakTilInnsending in setOf("Ny", "Endring")) """, "arsakTilInnsending": "${vedtak.arbeidsgiveropplysninger.arsakTilInnsending}"""" else ""}
            }
            """
            }
    }

fun List<Periode>.tilJson(): String = objectMapper.writeValueAsString(this)
