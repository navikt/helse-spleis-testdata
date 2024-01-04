package no.nav.helse.testdata.api

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.testdata.PdlClient
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.log
import no.nav.helse.testdata.sikkerlogg
import java.util.UUID

internal fun Routing.registerPersonApi(rapidsMediator: RapidsMediator, pdlClient: PdlClient) {
    delete("/person") {
        val fnr = call.request.header("ident")
        rapidsMediator.slett(fnr ?: throw IllegalArgumentException("Mangler ident"))
        log.info("produserte slettemelding, se sikkerlogg for fnr")
        sikkerlogg.info("produserte slettemelding for fnr=$fnr")

        call.respond(HttpStatusCode.OK)
    }
    get("/person/{ident}") {
        val ident = call.parameters["ident"] ?: throw IllegalArgumentException("mangler ident")
        val response = pdlClient.hentNavn(ident, UUID.randomUUID().toString())
        val data = response.path("data").path("hentPerson").path("navn").firstOrNull() ?: return@get call.respond(HttpStatusCode.NoContent)
        call.respond(
            PersonResponse(
                fornavn = data.path("fornavn").asText(),
                mellomnavn = data.path("mellomnavn").takeIf(JsonNode::isTextual)?.asText(),
                etternavn = data.path("etternavn").asText()
            )
        )
    }
}

data class PersonResponse(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String
)
