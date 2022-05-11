package no.nav.helse.testdata.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.testdata.AktørRestClient
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.Result

internal fun Routing.registerPersonApi(rapidsMediator: RapidsMediator, aktørRestClient: AktørRestClient) {
    delete("/person") {
        val fnr = call.request.header("ident")
        rapidsMediator.slett(fnr ?: throw IllegalArgumentException("Mangler ident"))
        call.respond(HttpStatusCode.OK)
    }
    get("/person/aktorid") {
        val fnr = call.request.header("ident")
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Mangler ident i requesten")

        return@get when (val res = aktørRestClient.hentAktørId(fnr)) {
            is Result.Ok -> call.respond(HttpStatusCode.OK, res.value)
            is Result.Error -> call.respond(HttpStatusCode.InternalServerError, "Feil")
        }
    }
}