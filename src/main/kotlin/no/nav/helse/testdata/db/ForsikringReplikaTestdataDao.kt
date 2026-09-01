package no.nav.helse.testdata.db

import com.fasterxml.jackson.annotation.JsonAutoDetect
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource
import kotliquery.Parameter
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language

// Nødvendig annotasjon for at Jackson beholder store bokstaver på feltene
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
)
data class IfVedfrivt10(
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
    val ID_VED: BigDecimal,
    val OPPDATERT: Instant?,
)

// Nødvendig annotasjon for at Jackson beholder store bokstaver på feltene
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
)
data class IfFkonto12(
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
    val ID_KONT: BigDecimal,
    val OPPDATERT: Instant?,
)

class ForsikringReplikaTestdataDao(private val dataSource: DataSource) {
    fun nesteIdVed(): BigDecimal = nesteSekvensverdi("ID_VED_SEQ")

    fun nesteIdKont(): BigDecimal = nesteSekvensverdi("ID_KONT_SEQ")

    fun lagreIfVedfrivt10(rad: IfVedfrivt10) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """
                INSERT INTO IF_VEDFRIVT_10 (
                    IF01_KODE, IF01_AGNR_FNR, IF10_FORSFOM_SEQ, IF10_GODKJ, IF10_FORSFOM,
                    IF10_VIRKDATO, IF10_TYPE, IF10_SELVFOM, IF10_KOMBI, IF10_PREMGRL,
                    IF10_FOM, IF10_PREMIE, IF10_GML_PREMGRL, IF10_GML_FOM, IF10_GML_PREMIE,
                    IF10_FRIFOM, IF10_FORSTOM, IF10_OPPHGR, IF10_VARSEL, IF10_TERM_KV,
                    IF10_TERM_AAR, IF10_VARSEL_BELOEP, IF10_BETALT_BELOEP, IF10_PURR,
                    IF10_TKNR_BOST, IF10_TKNR_BEH, OPPRETTET, ENDRET_I_KILDE, KILDE_IF,
                    ID_VED, OPPDATERT
                ) VALUES (
                    :IF01_KODE, :IF01_AGNR_FNR, :IF10_FORSFOM_SEQ, :IF10_GODKJ, :IF10_FORSFOM,
                    :IF10_VIRKDATO, :IF10_TYPE, :IF10_SELVFOM, :IF10_KOMBI, :IF10_PREMGRL,
                    :IF10_FOM, :IF10_PREMIE, :IF10_GML_PREMGRL, :IF10_GML_FOM, :IF10_GML_PREMIE,
                    :IF10_FRIFOM, :IF10_FORSTOM, :IF10_OPPHGR, :IF10_VARSEL, :IF10_TERM_KV,
                    :IF10_TERM_AAR, :IF10_VARSEL_BELOEP, :IF10_BETALT_BELOEP, :IF10_PURR,
                    :IF10_TKNR_BOST, :IF10_TKNR_BEH, :OPPRETTET, :ENDRET_I_KILDE, :KILDE_IF,
                    :ID_VED, :OPPDATERT
                )
            """
            session.run(queryOf(statement, parametere(rad)).asUpdate)
        }
    }

    fun hentIfVedfrivt10(ID_VED: BigDecimal): IfVedfrivt10? =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = "SELECT * FROM IF_VEDFRIVT_10 WHERE ID_VED = :ID_VED"
            session.run(queryOf(statement, mapOf("ID_VED" to ID_VED)).map(::ifVedfrivt10).asSingle)
        }

    fun finnIfVedfrivt10(): List<IfVedfrivt10> =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    // language=postgresql
                    "SELECT * FROM IF_VEDFRIVT_10 "
                ).map(::ifVedfrivt10).asList
            )
        }

    fun finnIfVedfrivt10(IF01_AGNR_FNR: Long): List<IfVedfrivt10> =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """
                SELECT * FROM IF_VEDFRIVT_10
                WHERE IF01_AGNR_FNR = :IF01_AGNR_FNR
                ORDER BY ID_VED
            """
            session.run(queryOf(statement, mapOf("IF01_AGNR_FNR" to IF01_AGNR_FNR)).map(::ifVedfrivt10).asList)
        }

    fun oppdaterIfVedfrivt10(rad: IfVedfrivt10): Boolean =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """
                UPDATE IF_VEDFRIVT_10 SET
                    IF01_KODE = :IF01_KODE,
                    IF01_AGNR_FNR = :IF01_AGNR_FNR,
                    IF10_FORSFOM_SEQ = :IF10_FORSFOM_SEQ,
                    IF10_GODKJ = :IF10_GODKJ,
                    IF10_FORSFOM = :IF10_FORSFOM,
                    IF10_VIRKDATO = :IF10_VIRKDATO,
                    IF10_TYPE = :IF10_TYPE,
                    IF10_SELVFOM = :IF10_SELVFOM,
                    IF10_KOMBI = :IF10_KOMBI,
                    IF10_PREMGRL = :IF10_PREMGRL,
                    IF10_FOM = :IF10_FOM,
                    IF10_PREMIE = :IF10_PREMIE,
                    IF10_GML_PREMGRL = :IF10_GML_PREMGRL,
                    IF10_GML_FOM = :IF10_GML_FOM,
                    IF10_GML_PREMIE = :IF10_GML_PREMIE,
                    IF10_FRIFOM = :IF10_FRIFOM,
                    IF10_FORSTOM = :IF10_FORSTOM,
                    IF10_OPPHGR = :IF10_OPPHGR,
                    IF10_VARSEL = :IF10_VARSEL,
                    IF10_TERM_KV = :IF10_TERM_KV,
                    IF10_TERM_AAR = :IF10_TERM_AAR,
                    IF10_VARSEL_BELOEP = :IF10_VARSEL_BELOEP,
                    IF10_BETALT_BELOEP = :IF10_BETALT_BELOEP,
                    IF10_PURR = :IF10_PURR,
                    IF10_TKNR_BOST = :IF10_TKNR_BOST,
                    IF10_TKNR_BEH = :IF10_TKNR_BEH,
                    OPPRETTET = :OPPRETTET,
                    ENDRET_I_KILDE = :ENDRET_I_KILDE,
                    KILDE_IF = :KILDE_IF,
                    OPPDATERT = :OPPDATERT
                WHERE ID_VED = :ID_VED
            """
            session.run(queryOf(statement, parametere(rad)).asUpdate) > 0
        }

    /**
     * Som [oppdaterIfVedfrivt10], men rører ikke OPPRETTET. Da beholder raden opprettelsestidspunktet
     * sitt uten at kallstedet må slå det opp først. OPPRETTET på [rad] blir derfor ignorert.
     */
    fun oppdaterIfVedfrivt10UtenOpprettet(rad: IfVedfrivt10): Boolean =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """
                UPDATE IF_VEDFRIVT_10 SET
                    IF01_KODE = :IF01_KODE,
                    IF01_AGNR_FNR = :IF01_AGNR_FNR,
                    IF10_FORSFOM_SEQ = :IF10_FORSFOM_SEQ,
                    IF10_GODKJ = :IF10_GODKJ,
                    IF10_FORSFOM = :IF10_FORSFOM,
                    IF10_VIRKDATO = :IF10_VIRKDATO,
                    IF10_TYPE = :IF10_TYPE,
                    IF10_SELVFOM = :IF10_SELVFOM,
                    IF10_KOMBI = :IF10_KOMBI,
                    IF10_PREMGRL = :IF10_PREMGRL,
                    IF10_FOM = :IF10_FOM,
                    IF10_PREMIE = :IF10_PREMIE,
                    IF10_GML_PREMGRL = :IF10_GML_PREMGRL,
                    IF10_GML_FOM = :IF10_GML_FOM,
                    IF10_GML_PREMIE = :IF10_GML_PREMIE,
                    IF10_FRIFOM = :IF10_FRIFOM,
                    IF10_FORSTOM = :IF10_FORSTOM,
                    IF10_OPPHGR = :IF10_OPPHGR,
                    IF10_VARSEL = :IF10_VARSEL,
                    IF10_TERM_KV = :IF10_TERM_KV,
                    IF10_TERM_AAR = :IF10_TERM_AAR,
                    IF10_VARSEL_BELOEP = :IF10_VARSEL_BELOEP,
                    IF10_BETALT_BELOEP = :IF10_BETALT_BELOEP,
                    IF10_PURR = :IF10_PURR,
                    IF10_TKNR_BOST = :IF10_TKNR_BOST,
                    IF10_TKNR_BEH = :IF10_TKNR_BEH,
                    ENDRET_I_KILDE = :ENDRET_I_KILDE,
                    KILDE_IF = :KILDE_IF,
                    OPPDATERT = :OPPDATERT
                WHERE ID_VED = :ID_VED
            """
            session.run(queryOf(statement, parametere(rad)).asUpdate) > 0
        }

    fun slettIfVedfrivt10(ID_VED: BigDecimal): Boolean =        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = "DELETE FROM IF_VEDFRIVT_10 WHERE ID_VED = :ID_VED"
            session.run(queryOf(statement, mapOf("ID_VED" to ID_VED)).asUpdate) > 0
        }

    fun lagreIfFkonto12(rad: IfFkonto12) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """
                INSERT INTO IF_FKONTO_12 (
                    IF01_KODE, IF01_AGNR_FNR, IF10_FORSFOM_SEQ, IF12_BETDATO_SEQ,
                    IF12_FOM, IF12_TOM, IF12_BET_KODE, IF12_FRIUKER, IF12_BELOEP,
                    IF12_BETDATO, OPPRETTET, ENDRET_I_KILDE, KILDE_IF, ID_KONT, OPPDATERT
                ) VALUES (
                    :IF01_KODE, :IF01_AGNR_FNR, :IF10_FORSFOM_SEQ, :IF12_BETDATO_SEQ,
                    :IF12_FOM, :IF12_TOM, :IF12_BET_KODE, :IF12_FRIUKER, :IF12_BELOEP,
                    :IF12_BETDATO, :OPPRETTET, :ENDRET_I_KILDE, :KILDE_IF, :ID_KONT, :OPPDATERT
                )
            """
            session.run(queryOf(statement, parametere(rad)).asUpdate)
        }
    }

    fun hentIfFkonto12(ID_KONT: BigDecimal): IfFkonto12? =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = "SELECT * FROM IF_FKONTO_12 WHERE ID_KONT = :ID_KONT"
            session.run(queryOf(statement, mapOf("ID_KONT" to ID_KONT)).map(::ifFkonto12).asSingle)
        }

    fun finnIfFkonto12(IF01_AGNR_FNR: Long?): List<IfFkonto12> =
        sessionOf(dataSource).use { session ->
            if (IF01_AGNR_FNR == null) {
                @Language("PostgreSQL")
                val statement = "SELECT * FROM IF_FKONTO_12 ORDER BY ID_KONT"
                session.run(queryOf(statement).map(::ifFkonto12).asList)
            } else {
                @Language("PostgreSQL")
                val statement = """
                    SELECT * FROM IF_FKONTO_12
                    WHERE IF01_AGNR_FNR = :IF01_AGNR_FNR
                    ORDER BY ID_KONT
                """
                session.run(queryOf(statement, mapOf("IF01_AGNR_FNR" to IF01_AGNR_FNR)).map(::ifFkonto12).asList)
            }
        }

    fun finnIfFkonto12(IF01_KODE: Char, IF01_AGNR_FNR: Long, IF10_FORSFOM_SEQ: Int): List<IfFkonto12> =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """
                SELECT * FROM IF_FKONTO_12
                WHERE IF01_KODE = :IF01_KODE
                  AND IF01_AGNR_FNR = :IF01_AGNR_FNR
                  AND IF10_FORSFOM_SEQ = :IF10_FORSFOM_SEQ
                ORDER BY IF12_FOM NULLS LAST, ID_KONT
            """
            session.run(
                queryOf(
                    statement,
                    mapOf(
                        "IF01_KODE" to IF01_KODE.toString(),
                        "IF01_AGNR_FNR" to IF01_AGNR_FNR,
                        "IF10_FORSFOM_SEQ" to IF10_FORSFOM_SEQ,
                    ),
                ).map(::ifFkonto12).asList,
            )
        }

    fun oppdaterIfFkonto12(rad: IfFkonto12): Boolean =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """
                UPDATE IF_FKONTO_12 SET
                    IF01_KODE = :IF01_KODE,
                    IF01_AGNR_FNR = :IF01_AGNR_FNR,
                    IF10_FORSFOM_SEQ = :IF10_FORSFOM_SEQ,
                    IF12_BETDATO_SEQ = :IF12_BETDATO_SEQ,
                    IF12_FOM = :IF12_FOM,
                    IF12_TOM = :IF12_TOM,
                    IF12_BET_KODE = :IF12_BET_KODE,
                    IF12_FRIUKER = :IF12_FRIUKER,
                    IF12_BELOEP = :IF12_BELOEP,
                    IF12_BETDATO = :IF12_BETDATO,
                    OPPRETTET = :OPPRETTET,
                    ENDRET_I_KILDE = :ENDRET_I_KILDE,
                    KILDE_IF = :KILDE_IF,
                    OPPDATERT = :OPPDATERT
                WHERE ID_KONT = :ID_KONT
            """
            session.run(queryOf(statement, parametere(rad)).asUpdate) > 0
        }

    /**
     * Som [oppdaterIfFkonto12], men rører ikke OPPRETTET. Da beholder raden opprettelsestidspunktet
     * sitt uten at kallstedet må slå det opp først. OPPRETTET på [rad] blir derfor ignorert.
     */
    fun oppdaterIfFkonto12UtenOpprettet(rad: IfFkonto12): Boolean =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """
                UPDATE IF_FKONTO_12 SET
                    IF01_KODE = :IF01_KODE,
                    IF01_AGNR_FNR = :IF01_AGNR_FNR,
                    IF10_FORSFOM_SEQ = :IF10_FORSFOM_SEQ,
                    IF12_BETDATO_SEQ = :IF12_BETDATO_SEQ,
                    IF12_FOM = :IF12_FOM,
                    IF12_TOM = :IF12_TOM,
                    IF12_BET_KODE = :IF12_BET_KODE,
                    IF12_FRIUKER = :IF12_FRIUKER,
                    IF12_BELOEP = :IF12_BELOEP,
                    IF12_BETDATO = :IF12_BETDATO,
                    ENDRET_I_KILDE = :ENDRET_I_KILDE,
                    KILDE_IF = :KILDE_IF,
                    OPPDATERT = :OPPDATERT
                WHERE ID_KONT = :ID_KONT
            """
            session.run(queryOf(statement, parametere(rad)).asUpdate) > 0
        }

    fun slettIfFkonto12(ID_KONT: BigDecimal): Boolean =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = "DELETE FROM IF_FKONTO_12 WHERE ID_KONT = :ID_KONT"
            session.run(queryOf(statement, mapOf("ID_KONT" to ID_KONT)).asUpdate) > 0
        }

    private fun nesteSekvensverdi(sekvens: String): BigDecimal =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = "SELECT nextval(CAST(:sekvens AS regclass)) AS NESTE"
            checkNotNull(
                session.run(
                    queryOf(statement, mapOf("sekvens" to sekvens)).map { it.bigDecimal("NESTE") }.asSingle,
                ),
            ) { "Fikk ingen verdi fra sekvensen $sekvens" }
        }

    private fun parametere(rad: IfVedfrivt10) = mapOf(
        "IF01_KODE" to rad.IF01_KODE.toString(),
        "IF01_AGNR_FNR" to rad.IF01_AGNR_FNR,
        "IF10_FORSFOM_SEQ" to rad.IF10_FORSFOM_SEQ,
        "IF10_GODKJ" to rad.IF10_GODKJ.toString(),
        "IF10_FORSFOM" to rad.IF10_FORSFOM,
        "IF10_VIRKDATO" to rad.IF10_VIRKDATO,
        "IF10_TYPE" to rad.IF10_TYPE.toString(),
        "IF10_SELVFOM" to rad.IF10_SELVFOM,
        "IF10_KOMBI" to rad.IF10_KOMBI.toString(),
        "IF10_PREMGRL" to rad.IF10_PREMGRL,
        "IF10_FOM" to rad.IF10_FOM,
        "IF10_PREMIE" to rad.IF10_PREMIE,
        "IF10_GML_PREMGRL" to rad.IF10_GML_PREMGRL,
        "IF10_GML_FOM" to rad.IF10_GML_FOM,
        "IF10_GML_PREMIE" to rad.IF10_GML_PREMIE,
        "IF10_FRIFOM" to rad.IF10_FRIFOM,
        "IF10_FORSTOM" to rad.IF10_FORSTOM,
        "IF10_OPPHGR" to rad.IF10_OPPHGR,
        "IF10_VARSEL" to rad.IF10_VARSEL,
        "IF10_TERM_KV" to rad.IF10_TERM_KV.toString(),
        "IF10_TERM_AAR" to rad.IF10_TERM_AAR,
        "IF10_VARSEL_BELOEP" to rad.IF10_VARSEL_BELOEP,
        "IF10_BETALT_BELOEP" to rad.IF10_BETALT_BELOEP,
        "IF10_PURR" to rad.IF10_PURR,
        "IF10_TKNR_BOST" to rad.IF10_TKNR_BOST,
        "IF10_TKNR_BEH" to rad.IF10_TKNR_BEH,
        "OPPRETTET" to Timestamp.from(rad.OPPRETTET),
        "ENDRET_I_KILDE" to Timestamp.from(rad.ENDRET_I_KILDE),
        "KILDE_IF" to rad.KILDE_IF,
        "ID_VED" to rad.ID_VED,
        "OPPDATERT" to Parameter(rad.OPPDATERT?.let { Timestamp.from(it) }, Timestamp::class.java),
    )

    private fun parametere(rad: IfFkonto12) = mapOf(
        "IF01_KODE" to Parameter(rad.IF01_KODE?.toString(), String::class.java),
        "IF01_AGNR_FNR" to Parameter(rad.IF01_AGNR_FNR, Long::class.java),
        "IF10_FORSFOM_SEQ" to Parameter(rad.IF10_FORSFOM_SEQ, Int::class.java),
        "IF12_BETDATO_SEQ" to Parameter(rad.IF12_BETDATO_SEQ, Int::class.java),
        "IF12_FOM" to Parameter(rad.IF12_FOM, Int::class.java),
        "IF12_TOM" to Parameter(rad.IF12_TOM, Int::class.java),
        "IF12_BET_KODE" to Parameter(rad.IF12_BET_KODE?.toString(), String::class.java),
        "IF12_FRIUKER" to Parameter(rad.IF12_FRIUKER, String::class.java),
        "IF12_BELOEP" to Parameter(rad.IF12_BELOEP, BigDecimal::class.java),
        "IF12_BETDATO" to Parameter(rad.IF12_BETDATO, Int::class.java),
        "OPPRETTET" to Timestamp.from(rad.OPPRETTET),
        "ENDRET_I_KILDE" to Timestamp.from(rad.ENDRET_I_KILDE),
        "KILDE_IF" to rad.KILDE_IF,
        "ID_KONT" to rad.ID_KONT,
        "OPPDATERT" to Parameter(rad.OPPDATERT?.let { Timestamp.from(it) }, Timestamp::class.java),
    )

    private fun ifVedfrivt10(row: Row) = IfVedfrivt10(
        IF01_KODE = row.string("IF01_KODE").single(),
        IF01_AGNR_FNR = row.long("IF01_AGNR_FNR"),
        IF10_FORSFOM_SEQ = row.int("IF10_FORSFOM_SEQ"),
        IF10_GODKJ = row.string("IF10_GODKJ").single(),
        IF10_FORSFOM = row.int("IF10_FORSFOM"),
        IF10_VIRKDATO = row.int("IF10_VIRKDATO"),
        IF10_TYPE = row.string("IF10_TYPE").single(),
        IF10_SELVFOM = row.string("IF10_SELVFOM"),
        IF10_KOMBI = row.string("IF10_KOMBI").single(),
        IF10_PREMGRL = row.int("IF10_PREMGRL"),
        IF10_FOM = row.int("IF10_FOM"),
        IF10_PREMIE = row.int("IF10_PREMIE"),
        IF10_GML_PREMGRL = row.int("IF10_GML_PREMGRL"),
        IF10_GML_FOM = row.int("IF10_GML_FOM"),
        IF10_GML_PREMIE = row.int("IF10_GML_PREMIE"),
        IF10_FRIFOM = row.int("IF10_FRIFOM"),
        IF10_FORSTOM = row.int("IF10_FORSTOM"),
        IF10_OPPHGR = row.string("IF10_OPPHGR"),
        IF10_VARSEL = row.int("IF10_VARSEL"),
        IF10_TERM_KV = row.string("IF10_TERM_KV").single(),
        IF10_TERM_AAR = row.string("IF10_TERM_AAR"),
        IF10_VARSEL_BELOEP = row.int("IF10_VARSEL_BELOEP"),
        IF10_BETALT_BELOEP = row.int("IF10_BETALT_BELOEP"),
        IF10_PURR = row.int("IF10_PURR"),
        IF10_TKNR_BOST = row.int("IF10_TKNR_BOST"),
        IF10_TKNR_BEH = row.int("IF10_TKNR_BEH"),
        OPPRETTET = row.instant("OPPRETTET"),
        ENDRET_I_KILDE = row.instant("ENDRET_I_KILDE"),
        KILDE_IF = row.string("KILDE_IF"),
        ID_VED = row.bigDecimal("ID_VED"),
        OPPDATERT = row.instantOrNull("OPPDATERT"),
    )

    private fun ifFkonto12(row: Row) = IfFkonto12(
        IF01_KODE = row.stringOrNull("IF01_KODE")?.single(),
        IF01_AGNR_FNR = row.longOrNull("IF01_AGNR_FNR"),
        IF10_FORSFOM_SEQ = row.intOrNull("IF10_FORSFOM_SEQ"),
        IF12_BETDATO_SEQ = row.intOrNull("IF12_BETDATO_SEQ"),
        IF12_FOM = row.intOrNull("IF12_FOM"),
        IF12_TOM = row.intOrNull("IF12_TOM"),
        IF12_BET_KODE = row.stringOrNull("IF12_BET_KODE")?.single(),
        IF12_FRIUKER = row.stringOrNull("IF12_FRIUKER"),
        IF12_BELOEP = row.bigDecimalOrNull("IF12_BELOEP"),
        IF12_BETDATO = row.intOrNull("IF12_BETDATO"),
        OPPRETTET = row.instant("OPPRETTET"),
        ENDRET_I_KILDE = row.instant("ENDRET_I_KILDE"),
        KILDE_IF = row.string("KILDE_IF"),
        ID_KONT = row.bigDecimal("ID_KONT"),
        OPPDATERT = row.instantOrNull("OPPDATERT"),
    )
}
