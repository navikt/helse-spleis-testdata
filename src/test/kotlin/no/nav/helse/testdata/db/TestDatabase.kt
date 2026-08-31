package no.nav.helse.testdata.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.math.BigDecimal
import java.time.Instant
import org.flywaydb.core.Flyway
import org.testcontainers.postgresql.PostgreSQLContainer

object TestDatabase {
    private val postgres by lazy { PostgreSQLContainer("postgres:17").apply { start() } }

    val dataSource: HikariDataSource by lazy {
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = postgres.jdbcUrl
                username = postgres.username
                password = postgres.password
            }
        ).also { dataSource ->
            Flyway
                .configure()
                .dataSource(dataSource)
                .locations("classpath:forsikring-replika-testdata/db/migrations")
                .load()
                .migrate()
        }
    }

    val dao: ForsikringReplikaTestdataDao by lazy { ForsikringReplikaTestdataDao(dataSource) }
}

fun ifVedfrivt10(
    IF01_KODE: Char = '1',
    IF01_AGNR_FNR: Long,
    IF10_FORSFOM_SEQ: Int = 0,
    IF10_GODKJ: Char = 'J',
    IF10_FORSFOM: Int = 0,
    IF10_VIRKDATO: Int = 20260101,
    IF10_TYPE: Char = '1',
    IF10_SELVFOM: String = " ",
    IF10_KOMBI: Char = ' ',
    IF10_PREMGRL: Int = 0,
    IF10_FOM: Int = 0,
    IF10_PREMIE: Int = 0,
    IF10_GML_PREMGRL: Int = 0,
    IF10_GML_FOM: Int = 0,
    IF10_GML_PREMIE: Int = 0,
    IF10_FRIFOM: Int = 0,
    IF10_FORSTOM: Int = 0,
    IF10_OPPHGR: String = " ",
    IF10_VARSEL: Int = 0,
    IF10_TERM_KV: Char = ' ',
    IF10_TERM_AAR: String = " ",
    IF10_VARSEL_BELOEP: Int = 0,
    IF10_BETALT_BELOEP: Int = 0,
    IF10_PURR: Int = 0,
    IF10_TKNR_BOST: Int = 0,
    IF10_TKNR_BEH: Int = 0,
    OPPRETTET: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    ENDRET_I_KILDE: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    KILDE_IF: String = " ",
    ID_VED: BigDecimal,
    OPPDATERT: Instant? = null,
) = IfVedfrivt10(
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

fun ifFkonto12(
    IF01_KODE: Char? = '1',
    IF01_AGNR_FNR: Long? = null,
    IF10_FORSFOM_SEQ: Int? = null,
    IF12_BETDATO_SEQ: Int? = null,
    IF12_FOM: Int? = null,
    IF12_TOM: Int? = null,
    IF12_BET_KODE: Char? = null,
    IF12_FRIUKER: String? = null,
    IF12_BELOEP: BigDecimal? = null,
    IF12_BETDATO: Int? = null,
    OPPRETTET: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    ENDRET_I_KILDE: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    KILDE_IF: String = " ",
    ID_KONT: BigDecimal,
    OPPDATERT: Instant? = null,
) = IfFkonto12(
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
