package no.nav.helse.testdata.api

import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
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
        mediator.connection.publish(behov.path("fødselsnummer").asText(), behov.toString())
        call.respond(HttpStatusCode.OK)
            .also { log.info("produsert data for behov: $behov") }
    }
}