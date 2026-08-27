package no.nav.helse.testdata.db

import java.math.BigDecimal
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource
import kotliquery.Parameter
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language

/**
 * DAO for å lagre testdata i forsikring-replika-testdata-databasen (Postgres).
 * Tabellene speiler kolonneoppsettet i Infotrygd sin IF_VEDFRIVT_10/IF_FKONTO_12,
 * jf. den ekte (Oracle-baserte) replikabasen sp-forsikring går mot.
 */
class ForsikringReplikaTestdataDao(private val dataSource: DataSource) {
    fun lagreIfVedfrivt10(
        IF01_KODE: Char,
        IF01_AGNR_FNR: Long,
        IF10_FORSFOM_SEQ: Int,
        IF10_GODKJ: Char,
        IF10_FORSFOM: Int,
        IF10_VIRKDATO: Int,
        IF10_TYPE: Char,
        IF10_SELVFOM: String,
        IF10_KOMBI: Char,
        IF10_PREMGRL: Int,
        IF10_FOM: Int,
        IF10_PREMIE: Int,
        IF10_GML_PREMGRL: Int,
        IF10_GML_FOM: Int,
        IF10_GML_PREMIE: Int,
        IF10_FRIFOM: Int,
        IF10_FORSTOM: Int,
        IF10_OPPHGR: String,
        IF10_VARSEL: Int,
        IF10_TERM_KV: Char,
        IF10_TERM_AAR: String,
        IF10_VARSEL_BELOEP: Int,
        IF10_BETALT_BELOEP: Int,
        IF10_PURR: Int,
        IF10_TKNR_BOST: Int,
        IF10_TKNR_BEH: Int,
        OPPRETTET: Instant,
        ENDRET_I_KILDE: Instant,
        KILDE_IF: String,
        ID_VED: BigDecimal,
        OPPDATERT: Instant?,
    ) {
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
            session.run(
                queryOf(
                    statement,
                    mapOf(
                        "IF01_KODE" to IF01_KODE.toString(),
                        "IF01_AGNR_FNR" to IF01_AGNR_FNR,
                        "IF10_FORSFOM_SEQ" to IF10_FORSFOM_SEQ,
                        "IF10_GODKJ" to IF10_GODKJ.toString(),
                        "IF10_FORSFOM" to IF10_FORSFOM,
                        "IF10_VIRKDATO" to IF10_VIRKDATO,
                        "IF10_TYPE" to IF10_TYPE.toString(),
                        "IF10_SELVFOM" to IF10_SELVFOM,
                        "IF10_KOMBI" to IF10_KOMBI.toString(),
                        "IF10_PREMGRL" to IF10_PREMGRL,
                        "IF10_FOM" to IF10_FOM,
                        "IF10_PREMIE" to IF10_PREMIE,
                        "IF10_GML_PREMGRL" to IF10_GML_PREMGRL,
                        "IF10_GML_FOM" to IF10_GML_FOM,
                        "IF10_GML_PREMIE" to IF10_GML_PREMIE,
                        "IF10_FRIFOM" to IF10_FRIFOM,
                        "IF10_FORSTOM" to IF10_FORSTOM,
                        "IF10_OPPHGR" to IF10_OPPHGR,
                        "IF10_VARSEL" to IF10_VARSEL,
                        "IF10_TERM_KV" to IF10_TERM_KV.toString(),
                        "IF10_TERM_AAR" to IF10_TERM_AAR,
                        "IF10_VARSEL_BELOEP" to IF10_VARSEL_BELOEP,
                        "IF10_BETALT_BELOEP" to IF10_BETALT_BELOEP,
                        "IF10_PURR" to IF10_PURR,
                        "IF10_TKNR_BOST" to IF10_TKNR_BOST,
                        "IF10_TKNR_BEH" to IF10_TKNR_BEH,
                        "OPPRETTET" to Timestamp.from(OPPRETTET),
                        "ENDRET_I_KILDE" to Timestamp.from(ENDRET_I_KILDE),
                        "KILDE_IF" to KILDE_IF,
                        "ID_VED" to ID_VED,
                        "OPPDATERT" to Parameter(OPPDATERT?.let { Timestamp.from(it) }, Timestamp::class.java),
                    ),
                ).asUpdate,
            )
        }
    }

    fun lagreIfFkonto12(
        IF01_KODE: Char,
        IF01_AGNR_FNR: Long?,
        IF10_FORSFOM_SEQ: Int?,
        IF12_BETDATO_SEQ: Int?,
        IF12_FOM: Int?,
        IF12_TOM: Int?,
        IF12_BET_KODE: Char?,
        IF12_FRIUKER: String?,
        IF12_BELOEP: BigDecimal?,
        IF12_BETDATO: Int?,
        OPPRETTET: Instant,
        ENDRET_I_KILDE: Instant,
        KILDE_IF: String,
        ID_KONT: BigDecimal,
        OPPDATERT: Instant?,
    ) {
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
            session.run(
                queryOf(
                    statement,
                    mapOf(
                        "IF01_KODE" to IF01_KODE.toString(),
                        "IF01_AGNR_FNR" to Parameter(IF01_AGNR_FNR, Long::class.java),
                        "IF10_FORSFOM_SEQ" to Parameter(IF10_FORSFOM_SEQ, Int::class.java),
                        "IF12_BETDATO_SEQ" to Parameter(IF12_BETDATO_SEQ, Int::class.java),
                        "IF12_FOM" to Parameter(IF12_FOM, Int::class.java),
                        "IF12_TOM" to Parameter(IF12_TOM, Int::class.java),
                        "IF12_BET_KODE" to Parameter(IF12_BET_KODE?.toString(), String::class.java),
                        "IF12_FRIUKER" to Parameter(IF12_FRIUKER, String::class.java),
                        "IF12_BELOEP" to Parameter(IF12_BELOEP, BigDecimal::class.java),
                        "IF12_BETDATO" to Parameter(IF12_BETDATO, Int::class.java),
                        "OPPRETTET" to Timestamp.from(OPPRETTET),
                        "ENDRET_I_KILDE" to Timestamp.from(ENDRET_I_KILDE),
                        "KILDE_IF" to KILDE_IF,
                        "ID_KONT" to ID_KONT,
                        "OPPDATERT" to Parameter(OPPDATERT?.let { Timestamp.from(it) }, Timestamp::class.java),
                    ),
                ).asUpdate,
            )
        }
    }
}
