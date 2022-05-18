package no.nav.helse.testdata.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.testdata.RapidsMediator

internal fun Routing.registerPersonApi(rapidsMediator: RapidsMediator) {
    delete("/person") {
        val fnr = call.request.header("ident")
        rapidsMediator.slett(fnr ?: throw IllegalArgumentException("Mangler ident"))
        call.respond(HttpStatusCode.OK)
    }
}