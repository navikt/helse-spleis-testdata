package no.nav.helse.testdata.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.helse.testdata.*
import java.util.*

internal fun Routing.registerOrganisasjonApi(eregClient: EregClient) = get("/organisasjon/{orgnr}") {
    val orgnr = requireNotNull(call.parameters["orgnr"])

    try {
        val response = OrganisasjonResponse(
            navn = eregClient.hentOrganisasjon(orgnr, UUID.randomUUID()).navn
        )
        call.respond(response)
    } catch (err: Exception) {
        log.error("feil ved oppslag i ereg: ${err.message}", err)
        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(err))
    }
}

data class OrganisasjonResponse(
    val navn: String
)