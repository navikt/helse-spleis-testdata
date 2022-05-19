package no.nav.helse.testdata

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.testdata.dto.DollyBruker
import no.nav.helse.testdata.dto.DollyIdentBestilling
import no.nav.helse.testdata.dto.DollyTestgruppe
import java.util.*

internal class DollyRestClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
) {
    suspend fun hentTestgruppe(id: String): Result<DollyTestgruppe, ResponseFailure> {
        return httpClient.get("$baseUrl/gruppe/$id") {
            val token = ""
            accept(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            header("Nav-Consumer-Id", "spleis-testdata")
            header("Nav-Call-Id", UUID.randomUUID().toString())
        }.let {
            Result.Ok(tilTestgruppe(objectMapper.readValue(it.body<String>())))
        }
    }
}

private fun tilTestgruppe(node: JsonNode): DollyTestgruppe {
    return DollyTestgruppe(
        id = node["id"].asInt(),
        navn = node["navn"].asText(),
        hensikt = node["hensikt"].asText(),
        opprettetAv = tilBruker(node["opprettetAv"]),
        sistEndretAv = tilBruker(node["sistEndretAv"]),
        tags = node["tags"].map {
            DollyTestgruppe.DollyTestGruppeTags.valueOf(it.asText())
        },
        datoEndret = node["datoEndret"].asLocalDate(),
        antallIdenter = node["antallIdenter"].asInt(),
        antallIBruk = node["antallIBruk"].asInt(),
        erEierAvGruppe = node["erEierAvGruppe"].asBoolean(),
        favorittIGruppen = node["favorittIGruppen"].asBoolean(),
        erLaast = node["erLaast"].asBoolean(),
        laastBeskrivelse = node["laastBeskrivelse"].asText(),
        identer = node["identer"].map {
            tilIdentBestilling(it)
        },
    )
}

private fun tilBruker(node: JsonNode): DollyBruker {
    return DollyBruker(
        brukerId = node["brukerId"].asText(),
        brukernavn = node["brukernavn"].asText(),
        brukertype = DollyBruker.DollyBrukertype.valueOf(node["brukertype"].asText()),
        epost = node["epost"].asText(),
        navIdent = node["navIdent"].asText(),
    )
}

private fun tilIdentBestilling(node: JsonNode): DollyIdentBestilling {
    return DollyIdentBestilling(
        ident = node["ident"].asText(),
        beskrivelse = node["beskrivelse"].asText(),
        bestillingId = node["bestillingId"].map {
            it.asInt()
        },
        master = DollyIdentBestilling.DollyIdentBestillingMaster.valueOf(node["master"].asText()),
        ibruk = node["ibruk"].asBoolean(),
    )
}