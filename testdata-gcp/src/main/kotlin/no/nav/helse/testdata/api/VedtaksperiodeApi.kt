package no.nav.helse.testdata.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.util.pipeline.PipelineContext
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.dokumenter.Vedtak
import no.nav.helse.testdata.dokumenter.inntektsmelding
import no.nav.helse.testdata.dokumenter.sykmelding
import no.nav.helse.testdata.dokumenter.søknad
import no.nav.helse.testdata.log


internal fun Route.registerVedtaksperiodeApi(mediator: RapidsMediator) {
    post("/vedtaksperiode") {
        val vedtak = call.receive<Vedtak>().also {
            validate(it)
        }

        val aktørId = "en-aktør" // TODO("Trenger å hente aktørId fra Dolly")

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
        log.info("produsert data for vedtak på aktør: $aktørId")
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
