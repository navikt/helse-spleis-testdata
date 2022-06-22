package no.nav.helse.testdata.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import no.nav.helse.testdata.AktørRestClient
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.Result
import no.nav.helse.testdata.dokumenter.Vedtak
import no.nav.helse.testdata.dokumenter.inntektsmelding
import no.nav.helse.testdata.dokumenter.sykmelding
import no.nav.helse.testdata.dokumenter.søknad
import no.nav.helse.testdata.log


internal fun Routing.registerVedtaksperiodeApi(mediator: RapidsMediator, aktørRestClient: AktørRestClient) {
    post("/vedtaksperiode") {
        val vedtak = call.receive<Vedtak>().also {
            validate(it)
        }

        val aktørIdResult = aktørRestClient.hentAktørId(vedtak.fnr)
        if (aktørIdResult is Result.Error) {
            log.info("fikk ikke slått opp aktørId for fødselsnummer ${vedtak.fnr}")
            call.respond(HttpStatusCode.InternalServerError, aktørIdResult.error.message!!)
            return@post
        }
        val aktørId = aktørIdResult.unwrap()

        sykmelding(vedtak, aktørId)?.also {
            log.info("produserer sykmelding på aktør: $aktørId\n$it")
            mediator.publiser(vedtak.fnr, it)
        }

        søknad(vedtak, aktørId)?.also {
            log.info("produserer søknad på aktør: $aktørId\n$it")
            mediator.publiser(vedtak.fnr, it)
        }

        inntektsmelding(vedtak, aktørId)?.also {
            log.info("produserer inntektsmelding på aktør: $aktørId\n$it")
            mediator.publiser(vedtak.fnr, it)
        }

        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for vedtak på aktør: $aktørId") }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.validate(
    vedtak: Vedtak,
) {
    if (vedtak.sykdomFom > vedtak.sykdomTom) {
        call.respond(HttpStatusCode.BadRequest, "FOM må være før TOM")
    }
}
