package no.nav.helse.testdata.api

import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.log


internal fun Routing.registerBehovApi(mediator: RapidsMediator) {
    post("/behov") {
        val behov = call.receive<ObjectNode>()
        behov.put("@event_name", "behov")
        if (!behov.path("@behov").isArray) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("fødselsnummer").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("organisasjonsnummer").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        if (!behov.path("vedtaksperiodeId").isTextual) return@post call.respond(HttpStatusCode.BadRequest)
        mediator.publiser(behov.path("fødselsnummer").asText(), behov.toString())
        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for behov: $behov") }
    }
}