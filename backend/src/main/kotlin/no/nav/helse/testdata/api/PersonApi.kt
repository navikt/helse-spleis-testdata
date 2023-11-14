package no.nav.helse.testdata.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.log
import no.nav.helse.testdata.sikkerlogg

internal fun Routing.registerPersonApi(rapidsMediator: RapidsMediator) {
    delete("/person") {
        val fnr = call.request.header("ident")
        rapidsMediator.slett(fnr ?: throw IllegalArgumentException("Mangler ident"))
        log.info("produserte slettemelding, se sikkerlogg for fnr")
        sikkerlogg.info("produserte slettemelding for fnr=$fnr")

        call.respond(HttpStatusCode.OK)
    }
}
