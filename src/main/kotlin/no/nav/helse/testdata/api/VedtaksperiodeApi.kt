package no.nav.helse.testdata.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
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
        val vedtak = call.receive<Vedtak>()
        val aktørIdResult = aktørRestClient.hentAktørId(vedtak.fnr)

        if (aktørIdResult is Result.Error) {
            call.respond(HttpStatusCode.InternalServerError, aktørIdResult.error.message!!)
            return@post
        }
        val aktørId = aktørIdResult.unwrap()

        sykmelding(vedtak, aktørId)?.also {
            log.info("produserer sykmelding på aktør: $aktørId\n$it")
            mediator.connection.publish(vedtak.fnr, it)
        }

        søknad(vedtak, aktørId)?.also {
            log.info("produserer søknad på aktør: $aktørId\n$it")
            mediator.connection.publish(vedtak.fnr, it)
        }

        inntektsmelding(vedtak, aktørId)?.also {
            log.info("produserer inntektsmelding på aktør: $aktørId\n$it")
            mediator.connection.publish(vedtak.fnr, it)
        }

        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for vedtak på aktør: $aktørId") }
    }
}