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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

    route("/personer/{identitetsnummer}/individuelle-forsikringer") {
        get {
            val IF01_AGNR_FNR = call.identitetsnummerParameter() ?: return@get
            call.respond(
                dao.finnIfVedfrivt10(IF01_AGNR_FNR).map(IndividuellForsikringResponse::fraRad).sortedWith(sortering)
            )
        }
        post {
            val IF01_AGNR_FNR = call.identitetsnummerParameter() ?: return@post
            val request = call.receiveEllerNull<IndividuellForsikringRequest>() ?: return@post
            val rad = request.tilRad(dao.nesteIdVed(), IF01_AGNR_FNR)
            dao.lagreIfVedfrivt10(rad)
            call.respond(HttpStatusCode.Created, IndividuellForsikringResponse.fraRad(rad))
        }
    }

    route("/individuelle-forsikringer/{ID_VED}") {
        get {
            val ID_VED = call.idParameter("ID_VED") ?: return@get
            val rad = dao.hentIfVedfrivt10(ID_VED) ?: return@get call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
            call.respond(IndividuellForsikringResponse.fraRad(rad))
        }
        put {
            val ID_VED = call.idParameter("ID_VED") ?: return@put
            val request = call.receiveEllerNull<IndividuellForsikringRequest>() ?: return@put
            // Forsikringen kan ikke flyttes til en annen person, så eieren hentes fra raden som ligger der
            val eksisterende = dao.hentIfVedfrivt10(ID_VED)
                ?: return@put call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
            // OPPRETTET blir ikke rørt av oppdateringen, så verdien vi setter her er uten betydning
            val rad = request.tilRad(ID_VED, eksisterende.IF01_AGNR_FNR)
            if (!dao.oppdaterIfVedfrivt10UtenOpprettet(rad)) {
                return@put call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
            }
            call.respond(IndividuellForsikringResponse.fraRad(checkNotNull(dao.hentIfVedfrivt10(ID_VED))))
        }
        delete {
            val ID_VED = call.idParameter("ID_VED") ?: return@delete
            if (!dao.slettIfVedfrivt10(ID_VED)) return@delete call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
            call.respond(HttpStatusCode.NoContent)
        }
        route("/forsikringsfakturaer") {
            get {
                val ID_VED = call.idParameter("ID_VED") ?: return@get
                val forsikring = dao.hentIfVedfrivt10(ID_VED)
                    ?: return@get call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
                call.respond(
                    dao.finnIfFkonto12(
                        forsikring.IF01_KODE,
                        forsikring.IF01_AGNR_FNR,
                        forsikring.IF10_FORSFOM_SEQ,
                    ).map(ForsikringsfakturaResponse::fraRad)
                )
            }
            post {
                val ID_VED = call.idParameter("ID_VED") ?: return@post
                val request = call.receiveEllerNull<ForsikringsfakturaRequest>() ?: return@post
                val forsikring = dao.hentIfVedfrivt10(ID_VED)
                    ?: return@post call.respondIkkeFunnet("IF_VEDFRIVT_10", ID_VED)
                val rad = request.tilRad(dao.nesteIdKont(), forsikring)
                    ?: return@post call.respondFeil(request.periodefeil())
                dao.lagreIfFkonto12(rad)
                call.respond(HttpStatusCode.Created, ForsikringsfakturaResponse.fraRad(rad))
            }
        }
    }

    route("/forsikringsfakturaer/{ID_KONT}") {
        get {
            val ID_KONT = call.idParameter("ID_KONT") ?: return@get
            val rad = dao.hentIfFkonto12(ID_KONT) ?: return@get call.respondIkkeFunnet("IF_FKONTO_12", ID_KONT)
            call.respond(ForsikringsfakturaResponse.fraRad(rad))
        }
        put {
            val ID_KONT = call.idParameter("ID_KONT") ?: return@put
            val request = call.receiveEllerNull<ForsikringsfakturaRequest>() ?: return@put
            // Fakturaen kan ikke flyttes til en annen forsikring, så nøklene hentes fra raden som ligger der
            val eksisterende = dao.hentIfFkonto12(ID_KONT)
                ?: return@put call.respondIkkeFunnet("IF_FKONTO_12", ID_KONT)
            // OPPRETTET blir ikke rørt av oppdateringen, så verdien vi setter her er uten betydning
            val rad = request.tilRad(ID_KONT, eksisterende)
                ?: return@put call.respondFeil(request.periodefeil())
            if (!dao.oppdaterIfFkonto12UtenOpprettet(rad)) {
                return@put call.respondIkkeFunnet("IF_FKONTO_12", ID_KONT)
            }
            call.respond(ForsikringsfakturaResponse.fraRad(checkNotNull(dao.hentIfFkonto12(ID_KONT))))
        }
        delete {
            val ID_KONT = call.idParameter("ID_KONT") ?: return@delete
            if (!dao.slettIfFkonto12(ID_KONT)) return@delete call.respondIkkeFunnet("IF_FKONTO_12", ID_KONT)
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

/**
 * Forenklet oppretting av en rad i IF_VEDFRIVT_10: bare feltene som betyr noe for en forsikring,
 * resten fylles med de nøytrale verdiene Infotrygd bruker. Personen forsikringen gjelder, kommer
 * fra stien.
 */
data class IndividuellForsikringRequest(
    val godkjent: Boolean,
    val fom: LocalDate,
    val virkningsdato: LocalDate,
    val type: IndividuellForsikringType,
    val premiegrunnlag: Int,
    val opphørsdato: LocalDate?,
    val opphørsgrunn: String?,
) {
    fun tilRad(ID_VED: BigDecimal, IF01_AGNR_FNR: Long): IfVedfrivt10 {
        val nå = Instant.now()
        return IfVedfrivt10(
            IF01_KODE = '1',
            IF01_AGNR_FNR = IF01_AGNR_FNR,
            IF10_FORSFOM_SEQ = ID_VED.toInt(),
            IF10_GODKJ = if (godkjent) 'J' else 'N',
            IF10_FORSFOM = fom.tilInfotrygddato(),
            IF10_VIRKDATO = virkningsdato.tilInfotrygddato(),
            IF10_TYPE = type.kode.digitToChar(),
            IF10_SELVFOM = " ",
            IF10_KOMBI = 'N',
            IF10_PREMGRL = premiegrunnlag,
            IF10_FOM = 0,
            IF10_PREMIE = 0,
            IF10_GML_PREMGRL = 0,
            IF10_GML_FOM = 0,
            IF10_GML_PREMIE = 0,
            IF10_FRIFOM = 0,
            IF10_FORSTOM = opphørsdato?.tilInfotrygddato() ?: 0,
            IF10_OPPHGR = opphørsgrunn ?: " ",
            IF10_VARSEL = 0,
            IF10_TERM_KV = ' ',
            IF10_TERM_AAR = " ",
            IF10_VARSEL_BELOEP = 0,
            IF10_BETALT_BELOEP = 0,
            IF10_PURR = 0,
            IF10_TKNR_BOST = 0,
            IF10_TKNR_BEH = 0,
            OPPRETTET = nå,
            ENDRET_I_KILDE = nå,
            KILDE_IF = " ",
            ID_VED = ID_VED,
            OPPDATERT = nå,
        )
    }
}

/**
 * Forenklet oppretting av en rad i IF_FKONTO_12: et halvår med en eventuell betalingsdato.
 * Forsikringen fakturaen hører til, kommer fra stien.
 */
data class ForsikringsfakturaRequest(
    val år: Int,
    val halvdel: Int,
    val betalingsdato: LocalDate?,
) {
    fun tilRad(ID_KONT: BigDecimal, forsikring: IfVedfrivt10) = tilRad(
        ID_KONT = ID_KONT,
        IF01_KODE = forsikring.IF01_KODE,
        IF01_AGNR_FNR = forsikring.IF01_AGNR_FNR,
        IF10_FORSFOM_SEQ = forsikring.IF10_FORSFOM_SEQ,
    )

    fun tilRad(ID_KONT: BigDecimal, eksisterende: IfFkonto12) = tilRad(
        ID_KONT = ID_KONT,
        IF01_KODE = eksisterende.IF01_KODE,
        IF01_AGNR_FNR = eksisterende.IF01_AGNR_FNR,
        IF10_FORSFOM_SEQ = eksisterende.IF10_FORSFOM_SEQ,
    )

    fun periodefeil() =
        "år må være mellom 1000 og 9999 og halvdel må være 1 eller 2, var år=$år og halvdel=$halvdel"

    /** Returnerer null om halvåret ikke lar seg regne ut, jf. [periodefeil]. */
    private fun tilRad(
        ID_KONT: BigDecimal,
        IF01_KODE: Char?,
        IF01_AGNR_FNR: Long?,
        IF10_FORSFOM_SEQ: Int?,
    ): IfFkonto12? {
        val (fom, tom) = halvår() ?: return null
        val nå = Instant.now()
        return IfFkonto12(
            IF01_KODE = IF01_KODE,
            IF01_AGNR_FNR = IF01_AGNR_FNR,
            IF10_FORSFOM_SEQ = IF10_FORSFOM_SEQ,
            IF12_BETDATO_SEQ = ID_KONT.toInt(),
            IF12_FOM = fom.tilInfotrygddato(),
            IF12_TOM = tom.tilInfotrygddato(),
            IF12_BET_KODE = if (betalingsdato != null) 'B' else ' ',
            IF12_FRIUKER = null,
            IF12_BELOEP = null,
            IF12_BETDATO = betalingsdato?.tilInfotrygddato(),
            OPPRETTET = nå,
            ENDRET_I_KILDE = nå,
            KILDE_IF = " ",
            ID_KONT = ID_KONT,
            OPPDATERT = nå,
        )
    }

    private fun halvår(): Pair<LocalDate, LocalDate>? = when {
        år !in 1000..9999 -> null
        halvdel == 1 -> LocalDate.of(år, 1, 1) to LocalDate.of(år, 6, 30)
        halvdel == 2 -> LocalDate.of(år, 7, 1) to LocalDate.of(år, 12, 31)
        else -> null
    }
}

data class Feilmelding(val melding: String)

/**
 * Den forenklede visningen av en rad i IF_VEDFRIVT_10. Felter som ikke lar seg tolke — for eksempel
 * en dato lagret som 0 eller en ukjent forsikringstype — kommer ut som null.
 */
data class IndividuellForsikringResponse(
    val id: BigDecimal,
    val identitetsnummer: String,
    val godkjent: Boolean,
    val fom: LocalDate?,
    val virkningsdato: LocalDate?,
    val type: IndividuellForsikringType?,
    val premiegrunnlag: Int,
    val opphørsdato: LocalDate?,
    val opphørsgrunn: String?,
) {
    companion object {
        fun fraRad(rad: IfVedfrivt10) = IndividuellForsikringResponse(
            id = rad.ID_VED,
            identitetsnummer = rad.IF01_AGNR_FNR.tilIdentitetsnummer(),
            godkjent = rad.IF10_GODKJ == 'J',
            fom = rad.IF10_FORSFOM.tilLocalDate(),
            virkningsdato = rad.IF10_VIRKDATO.tilLocalDate(),
            type = IndividuellForsikringType.fraKode(rad.IF10_TYPE),
            premiegrunnlag = rad.IF10_PREMGRL,
            opphørsdato = rad.IF10_FORSTOM.tilLocalDate(),
            opphørsgrunn = rad.IF10_OPPHGR.takeIf { it.isNotBlank() },
        )
    }
}

/**
 * Den forenklede visningen av en rad i IF_FKONTO_12. Halvåret leses ut av IF12_FOM, og er null
 * dersom raden ikke har en fra-dato som lar seg tolke.
 */
data class ForsikringsfakturaResponse(
    val id: BigDecimal,
    val år: Int?,
    val halvdel: Int?,
    val betalingsdato: LocalDate?,
) {
    companion object {
        fun fraRad(rad: IfFkonto12): ForsikringsfakturaResponse {
            val fom = rad.IF12_FOM?.tilLocalDate()
            return ForsikringsfakturaResponse(
                id = rad.ID_KONT,
                år = fom?.year,
                halvdel = fom?.let { if (it.monthValue <= 6) 1 else 2 },
                betalingsdato = rad.IF12_BETDATO?.tilLocalDate(),
            )
        }
    }
}

/** Sorterer på virkningsdato, deretter opphørsdato. Forsikringer uten dato kommer sist. */
private val sortering = compareBy<IndividuellForsikringResponse, LocalDate?>(nullsLast()) { it.virkningsdato }
    .thenBy(nullsLast()) { it.opphørsdato }

/** Infotrygd lagrer datoer som heltall på formen yyyyMMdd, og bruker 0 for «ingen dato». */
private fun LocalDate.tilInfotrygddato(): Int = format(DateTimeFormatter.BASIC_ISO_DATE).toInt()

private fun Int.tilLocalDate(): LocalDate? =
    if (this == 0) null
    else try {
        LocalDate.parse(toString().padStart(8, '0'), DateTimeFormatter.BASIC_ISO_DATE)
    } catch (err: DateTimeParseException) {
        log.info("klarte ikke tolke infotrygddatoen $this: ${err.message}", err)
        null
    }

/** Infotrygd lagrer identitetsnummer med snudd fødselsdato: ddMMyy blir yyMMdd. */
private fun String.tilAgnrFnr(): Long? {
    if (length != 11 || any { !it.isDigit() }) return null
    val dd = substring(0, 2)
    val MM = substring(2, 4)
    val yy = substring(4, 6)
    return "$yy$MM$dd${substring(6)}".toLong()
}

private fun Long.tilIdentitetsnummer(): String {
    val agnrFnr = toString().padStart(11, '0')
    val yy = agnrFnr.substring(0, 2)
    val MM = agnrFnr.substring(2, 4)
    val dd = agnrFnr.substring(4, 6)
    return "$dd$MM$yy${agnrFnr.substring(6)}"
}

private suspend fun ApplicationCall.identitetsnummerParameter(): Long? {
    val verdi = requireNotNull(parameters["identitetsnummer"])
    return verdi.tilAgnrFnr() ?: run {
        respondFeil("identitetsnummer må bestå av elleve siffer, var '$verdi'")
        null
    }
}

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
