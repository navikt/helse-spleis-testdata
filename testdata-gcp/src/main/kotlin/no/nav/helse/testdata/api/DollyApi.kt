package no.nav.helse.testdata.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.testdata.DollyRestClient
import no.nav.helse.testdata.Result

internal fun Route.registerDollyApi(dollyRestClient: DollyRestClient) {
    get("/gruppe/{id}") {
        val id = call.parameters["id"]

        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Mangler gruppeId")
            return@get
        }

        val testgruppe = dollyRestClient.hentTestgruppe(id)

        when (testgruppe) {
            is Result.Ok -> {
                call.respond(testgruppe)
            }
            is Result.Error -> {
                call.respond(testgruppe.error.statusCode, testgruppe.error.response)
            }
        }
    }
}