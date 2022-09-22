package no.nav.helse.testdata.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.dokumenter.Vedtak
import no.nav.helse.testdata.dokumenter.inntektsmelding
import no.nav.helse.testdata.dokumenter.sykmelding
import no.nav.helse.testdata.dokumenter.søknad
import no.nav.helse.testdata.log

internal fun Routing.registerVedtaksperiodeApi(mediator: RapidsMediator) {
    post("/vedtaksperiode") {
        val vedtak = call.receive<Vedtak>().also {
            validate(it)
        }

        sykmelding(vedtak)?.also {
            log.info("produserer sykmelding")
            mediator.publiser(vedtak.fnr, it)
        }

        søknad(vedtak)?.also {
            log.info("produserer søknad")
            mediator.publiser(vedtak.fnr, it)
        }

        inntektsmelding(vedtak)?.also {
            log.info("produserer inntektsmelding")
            mediator.publiser(vedtak.fnr, it)
        }

        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for vedtak") }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.validate(
    vedtak: Vedtak,
) {
    if (vedtak.sykdomFom > vedtak.sykdomTom) {
        call.respond(HttpStatusCode.BadRequest, "FOM må være før TOM")
    }
    vedtak.inntektsmelding?.arbeidsgiverperiode?.map {
        val (fom, tom) = it
        if (fom > tom) {
            call.respond(HttpStatusCode.BadRequest, "Arbeidsgiverperioder: FOM $fom må være før TOM $tom")
            return
        }
    }
}
