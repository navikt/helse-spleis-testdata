package no.nav.helse.testdata.api

import com.fasterxml.jackson.annotation.JsonAutoDetect
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.math.BigDecimal
import java.time.Instant
import no.nav.helse.testdata.db.ForsikringReplikaTestdataDao
import no.nav.helse.testdata.db.IfFkonto12
import no.nav.helse.testdata.db.IfVedfrivt10
import no.nav.helse.testdata.log

internal fun Routing.registerForsikringReplikaApi(dao: ForsikringReplikaTestdataDao) {
    route("/replikabase/if-vedfrivt-10") {
        get {
            call.respond(
                call.request.queryParameters["IF01_AGNR_FNR"]?.toLong()
                    ?.let { dao.finnIfVedfrivt10(it) }
                    ?: dao.finnIfVedfrivt10()
            )
        }
        post {
            val request = call.receive<IfVedfrivt10Request>()
            if (request.ID_VED != null) return@post call.respondFeil("ID_VED tildeles av databasen og kan ikke oppgis ved oppretting")
            val rad = request.tilRad(dao.nesteIdVed())
            dao.lagreIfVedfrivt10(rad)
            call.respond(HttpStatusCode.Created, rad)
        }
        get("/{ID_VED}") {
            val ID_VED = call.idParameter("ID_VED") ?: return@get
            val rad = dao.hentIfVedfrivt10(ID_VED) ?: return@get call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
            call.respond(rad)
        }
        put("/{ID_VED}") {
            val ID_VED = call.idParameter("ID_VED") ?: return@put
            val request = call.receiveEllerNull<IfVedfrivt10Request>() ?: return@put
            if (request.ID_VED != null && request.ID_VED.compareTo(ID_VED) != 0) {
                return@put call.respondFeil("ID_VED i kroppen (${request.ID_VED}) stemmer ikke med ID_VED i URL-en ($ID_VED)")
            }
            if (!dao.oppdaterIfVedfrivt10(request.tilRad(ID_VED))) return@put call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
            call.respond(checkNotNull(dao.hentIfVedfrivt10(ID_VED)))
        }
        delete("/{ID_VED}") {
            val ID_VED = call.idParameter("ID_VED") ?: return@delete
            if (!dao.slettIfVedfrivt10(ID_VED)) return@delete call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/replikabase/if-fkonto-12") {
        get {
            val IF01_AGNR_FNR = call.request.queryParameters["IF01_AGNR_FNR"]
            val agnrFnr = if (IF01_AGNR_FNR == null) null else IF01_AGNR_FNR.toLongOrNull()
                ?: return@get call.respondFeil("IF01_AGNR_FNR må være et heltall, var '$IF01_AGNR_FNR'")
            call.respond(dao.finnIfFkonto12(agnrFnr))
        }
        post {
            val request = call.receiveEllerNull<IfFkonto12Request>() ?: return@post
            if (request.ID_KONT != null) return@post call.respondFeil("ID_KONT tildeles av databasen og kan ikke oppgis ved oppretting")
            val rad = request.tilRad(dao.nesteIdKont())
            dao.lagreIfFkonto12(rad)
            call.respond(HttpStatusCode.Created, rad)
        }
        get("/{ID_KONT}") {
            val ID_KONT = call.idParameter("ID_KONT") ?: return@get
            val rad = dao.hentIfFkonto12(ID_KONT) ?: return@get call.respondIkkeFunnet("IF_FKONTO_12", ID_KONT)
            call.respond(rad)
        }
        put("/{ID_KONT}") {
            val ID_KONT = call.idParameter("ID_KONT") ?: return@put
            val request = call.receiveEllerNull<IfFkonto12Request>() ?: return@put
            if (request.ID_KONT != null && request.ID_KONT.compareTo(ID_KONT) != 0) {
                return@put call.respondFeil("ID_KONT i kroppen (${request.ID_KONT}) stemmer ikke med ID_KONT i URL-en ($ID_KONT)")
            }
            if (!dao.oppdaterIfFkonto12(request.tilRad(ID_KONT))) return@put call.respondIkkeFunnet("IF_FKONTO_12", ID_KONT)
            call.respond(checkNotNull(dao.hentIfFkonto12(ID_KONT)))
        }
        delete("/{ID_KONT}") {
            val ID_KONT = call.idParameter("ID_KONT") ?: return@delete
            if (!dao.slettIfFkonto12(ID_KONT)) return@delete call.respondIkkeFunnet("IF_FKONTO_12", ID_KONT)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

/** Jackson må bruke feltnavnene direkte, jf. kommentaren på [IfVedfrivt10]. */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
)
data class IfVedfrivt10Request(
    val IF01_KODE: Char,
    val IF01_AGNR_FNR: Long,
    val IF10_FORSFOM_SEQ: Int,
    val IF10_GODKJ: Char,
    val IF10_FORSFOM: Int,
    val IF10_VIRKDATO: Int,
    val IF10_TYPE: Char,
    val IF10_SELVFOM: String,
    val IF10_KOMBI: Char,
    val IF10_PREMGRL: Int,
    val IF10_FOM: Int,
    val IF10_PREMIE: Int,
    val IF10_GML_PREMGRL: Int,
    val IF10_GML_FOM: Int,
    val IF10_GML_PREMIE: Int,
    val IF10_FRIFOM: Int,
    val IF10_FORSTOM: Int,
    val IF10_OPPHGR: String,
    val IF10_VARSEL: Int,
    val IF10_TERM_KV: Char,
    val IF10_TERM_AAR: String,
    val IF10_VARSEL_BELOEP: Int,
    val IF10_BETALT_BELOEP: Int,
    val IF10_PURR: Int,
    val IF10_TKNR_BOST: Int,
    val IF10_TKNR_BEH: Int,
    val OPPRETTET: Instant,
    val ENDRET_I_KILDE: Instant,
    val KILDE_IF: String,
    val OPPDATERT: Instant?,
    val ID_VED: BigDecimal?,
) {
    fun tilRad(ID_VED: BigDecimal) = IfVedfrivt10(
        IF01_KODE = IF01_KODE,
        IF01_AGNR_FNR = IF01_AGNR_FNR,
        IF10_FORSFOM_SEQ = IF10_FORSFOM_SEQ,
        IF10_GODKJ = IF10_GODKJ,
        IF10_FORSFOM = IF10_FORSFOM,
        IF10_VIRKDATO = IF10_VIRKDATO,
        IF10_TYPE = IF10_TYPE,
        IF10_SELVFOM = IF10_SELVFOM,
        IF10_KOMBI = IF10_KOMBI,
        IF10_PREMGRL = IF10_PREMGRL,
        IF10_FOM = IF10_FOM,
        IF10_PREMIE = IF10_PREMIE,
        IF10_GML_PREMGRL = IF10_GML_PREMGRL,
        IF10_GML_FOM = IF10_GML_FOM,
        IF10_GML_PREMIE = IF10_GML_PREMIE,
        IF10_FRIFOM = IF10_FRIFOM,
        IF10_FORSTOM = IF10_FORSTOM,
        IF10_OPPHGR = IF10_OPPHGR,
        IF10_VARSEL = IF10_VARSEL,
        IF10_TERM_KV = IF10_TERM_KV,
        IF10_TERM_AAR = IF10_TERM_AAR,
        IF10_VARSEL_BELOEP = IF10_VARSEL_BELOEP,
        IF10_BETALT_BELOEP = IF10_BETALT_BELOEP,
        IF10_PURR = IF10_PURR,
        IF10_TKNR_BOST = IF10_TKNR_BOST,
        IF10_TKNR_BEH = IF10_TKNR_BEH,
        OPPRETTET = OPPRETTET,
        ENDRET_I_KILDE = ENDRET_I_KILDE,
        KILDE_IF = KILDE_IF,
        ID_VED = ID_VED,
        OPPDATERT = OPPDATERT,
    )
}

/** Jackson må bruke feltnavnene direkte, jf. kommentaren på [IfVedfrivt10]. */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
)
data class IfFkonto12Request(
    val IF01_KODE: Char?,
    val IF01_AGNR_FNR: Long?,
    val IF10_FORSFOM_SEQ: Int?,
    val IF12_BETDATO_SEQ: Int?,
    val IF12_FOM: Int?,
    val IF12_TOM: Int?,
    val IF12_BET_KODE: Char?,
    val IF12_FRIUKER: String?,
    val IF12_BELOEP: BigDecimal?,
    val IF12_BETDATO: Int?,
    val OPPRETTET: Instant,
    val ENDRET_I_KILDE: Instant,
    val KILDE_IF: String,
    val OPPDATERT: Instant?,
    val ID_KONT: BigDecimal?,
) {
    fun tilRad(ID_KONT: BigDecimal) = IfFkonto12(
        IF01_KODE = IF01_KODE,
        IF01_AGNR_FNR = IF01_AGNR_FNR,
        IF10_FORSFOM_SEQ = IF10_FORSFOM_SEQ,
        IF12_BETDATO_SEQ = IF12_BETDATO_SEQ,
        IF12_FOM = IF12_FOM,
        IF12_TOM = IF12_TOM,
        IF12_BET_KODE = IF12_BET_KODE,
        IF12_FRIUKER = IF12_FRIUKER,
        IF12_BELOEP = IF12_BELOEP,
        IF12_BETDATO = IF12_BETDATO,
        OPPRETTET = OPPRETTET,
        ENDRET_I_KILDE = ENDRET_I_KILDE,
        KILDE_IF = KILDE_IF,
        ID_KONT = ID_KONT,
        OPPDATERT = OPPDATERT,
    )
}

data class Feilmelding(val melding: String)

private suspend fun ApplicationCall.idParameter(navn: String): BigDecimal? {
    val verdi = requireNotNull(parameters[navn])
    return verdi.toBigDecimalOrNull() ?: run {
        respondFeil("$navn må være et tall, var '$verdi'")
        null
    }
}

private suspend inline fun <reified T : Any> ApplicationCall.receiveEllerNull(): T? =
    try {
        receive<T>()
    } catch (err: Exception) {
        log.info("klarte ikke lese forespørselen: ${err.message}", err)
        respondFeil("Ugyldig forespørsel: ${err.message}")
        null
    }

private suspend fun ApplicationCall.respondFeil(melding: String) =
    respond(HttpStatusCode.BadRequest, Feilmelding(melding))

private suspend fun ApplicationCall.respondIkkeFunnet(tabell: String, id: BigDecimal) =
    respond(HttpStatusCode.NotFound, Feilmelding("Fant ingen rad i $tabell med id $id"))
