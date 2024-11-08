package no.nav.helse.testdata.api

import com.github.navikt.tbd_libs.result_object.getOrThrow
import com.github.navikt.tbd_libs.speed.SpeedClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.testdata.RapidsMediator
import no.nav.helse.testdata.log
import no.nav.helse.testdata.sikkerlogg
import java.util.UUID

internal fun Routing.registerPersonApi(rapidsMediator: RapidsMediator, speedClient: SpeedClient) {
    delete("/person") {
        val fnr = call.request.header("ident")
        rapidsMediator.slett(fnr ?: throw IllegalArgumentException("Mangler ident"))
        log.info("produserte slettemelding, se sikkerlogg for fnr")
        sikkerlogg.info("produserte slettemelding for fnr=$fnr")

        call.respond(HttpStatusCode.OK)
    }
    get("/person/{ident}") {
        val ident = call.parameters["ident"] ?: throw IllegalArgumentException("mangler ident")
        val response = speedClient.hentPersoninfo(ident, UUID.randomUUID().toString()).getOrThrow()
        call.respond(
            PersonResponse(
                fornavn = response.fornavn,
                mellomnavn = response.mellomnavn,
                etternavn = response.etternavn
            )
        )
    }
}

data class PersonResponse(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String
)
