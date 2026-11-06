package no.nav.helse.testdata.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.post
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.dokumenter.Forhåndsvurdering
import no.nav.helse.testdata.dokumenter.Vedtak
import no.nav.helse.testdata.dokumenter.arbeidsgiveropplysninger
import no.nav.helse.testdata.dokumenter.sykmelding
import no.nav.helse.testdata.dokumenter.søknad
import no.nav.helse.testdata.gjeldendeTraceUrl
import no.nav.helse.testdata.log
import no.nav.helse.testdata.objectMapper
import no.nav.helse.testdata.sikkerlogg
import java.util.UUID

internal fun Routing.registerVedtaksperiodeApi(mediator: RapidsMediator) {
    post("/vedtaksperiode") {
        val vedtak: Vedtak =
            try {
                call.receive<Vedtak>().also {
                    validate(it)
                }
            } catch (e: Exception) {
                log.warn("Feil i input, lar seg ikke deserialisere", e)
                throw e
            }

        val fnr = vedtak.fnr

        // Arbeidsgiversvaret postes også hit, uten medlemskapVerdi, og skal ikke overskrive valgt vurdering
        vedtak.medlemskapVerdi?.also {
            val medlemskapavklaring = objectMapper.writeValueAsString(medlemskapavklaring(vedtak, it))
            sikkerlogg.info("publiserer mock_medlemskap_avklaring for $fnr: $medlemskapavklaring")
            mediator.publiser(fnr, medlemskapavklaring)
        }

        sykmelding(vedtak)?.also {
            log.info("produserer sykmelding, se sikkerlogg/tjenestekall for fnr")
            sikkerlogg.info("produserer sykmelding for fnr=$fnr")
            mediator.publiser(fnr, it)
        }

        val søknadId = UUID.randomUUID()
        // Sendes før søknaden, slik at sp-medlemskap har forhåndsvurderingen når Medlemskapsvurdering-behovet kommer
        vedtak.forhåndsvurdering?.takeIf { vedtak.søknad != null }?.also {
            val forhåndsvurdering = objectMapper.writeValueAsString(forhåndsvurdering(vedtak, it, søknadId))
            log.info("produserer forhåndsvurdering av medlemskap, se sikkerlogg/tjenestekall for fnr")
            sikkerlogg.info("produserer forhåndsvurdering av medlemskap for fnr=$fnr: $forhåndsvurdering")
            mediator.publiser(fnr, forhåndsvurdering)
        }

        søknad(vedtak, søknadId)?.also {
            log.info("produserer søknad, se sikkerlogg/tjenestekall for fnr")
            sikkerlogg.info("produserer søknad for fnr=$fnr")
            mediator.publiser(fnr, it)
        }

        arbeidsgiveropplysninger(vedtak)?.also {
            log.info("produserer arbeidsgiveropplysninger, se sikkerlogg/tjenestekall for fnr")
            sikkerlogg.info("produserer arbeidsgiveropplysninger for fnr=$fnr")
            mediator.publiser(fnr, it)
        }

        call
            .respond(HttpStatusCode.OK, SendteDokumenterResponse(traceUrl = gjeldendeTraceUrl()))
            .also {
                log.info("produsert dokumenter, se sikkerlogg/tjenestekall for fnr")
                sikkerlogg.info("produsert dokumenter for fnr=$fnr")
            }
    }
}

private const val IKKE_MOCK_MEDLEMSKAP = "IKKE_MOCK"

// Med IKKE_MOCK svarer ikke medlemskap-mocken, slik at sparkel-medlemskap slår opp i LovMe
private fun medlemskapavklaring(
    vedtak: Vedtak,
    medlemskapVerdi: String,
): Map<String, Any> =
    if (medlemskapVerdi == IKKE_MOCK_MEDLEMSKAP) {
        mapOf(
            "@event_name" to "mock_medlemskap_avklaring",
            "ident" to vedtak.fnr,
            "skalMockes" to false,
        )
    } else {
        mapOf(
            "@event_name" to "mock_medlemskap_avklaring",
            "ident" to vedtak.fnr,
            "medlemskapVerdi" to medlemskapVerdi,
            "aarsaker" to vedtak.medlemskapAarsaker.takeUnless { medlemskapVerdi == "JA" }.orEmpty(),
        )
    }

// Samme format som LovMe sender på medlemskap.medlemskapsvurderinger. Uten @event_name, slik sp-medlemskap krever.
// Søknaden har startSyketilfelle = sykdomFom, så vurderingen gjelder skjæringstidspunkter 16 dager før og etter den datoen.
private fun forhåndsvurdering(
    vedtak: Vedtak,
    forhåndsvurdering: Forhåndsvurdering,
    søknadId: UUID,
): Map<String, Any> =
    mapOf(
        "soknadId" to søknadId,
        "fnr" to vedtak.fnr,
        "speilSvar" to forhåndsvurdering.svar,
        "aarsaker" to forhåndsvurdering.aarsaker.takeUnless { forhåndsvurdering.svar == "JA" }.orEmpty(),
        "gyldigForSkjaeringstidspunktFom" to vedtak.sykdomFom.minusDays(GYLDIGHET_DAGER),
        "gyldigForSkjaeringstidspunktTom" to vedtak.sykdomFom.plusDays(GYLDIGHET_DAGER),
    )

private const val GYLDIGHET_DAGER = 16L

internal data class SendteDokumenterResponse(
    val traceUrl: String?,
)

private suspend fun RoutingContext.validate(
    vedtak: Vedtak,
) {
    if (vedtak.sykdomFom > vedtak.sykdomTom) {
        call.respond(HttpStatusCode.BadRequest, "FOM må være før TOM")
    }
    vedtak.arbeidsgiveropplysninger?.arbeidsgiverperiode?.map {
        val (fom, tom) = it
        if (fom > tom) {
            call.respond(HttpStatusCode.BadRequest, "Arbeidsgiverperioder: FOM $fom må være før TOM $tom")
            return
        }
    }
}
