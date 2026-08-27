package no.nav.helse.testdata.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.math.BigDecimal
import java.time.Instant
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer

class ForsikringReplikaTestdataDaoTest {
    companion object {
        private val postgres = PostgreSQLContainer("postgres:17").apply { start() }
        private lateinit var dataSource: HikariDataSource
        private lateinit var dao: ForsikringReplikaTestdataDao

        @BeforeAll
        @JvmStatic
        fun setup() {
            dataSource = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = postgres.jdbcUrl
                    username = postgres.username
                    password = postgres.password
                }
            )
            Flyway
                .configure()
                .dataSource(dataSource)
                .locations("classpath:forsikring-replika-testdata/db/migrations")
                .load()
                .migrate()
            dao = ForsikringReplikaTestdataDao(dataSource)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            dataSource.close()
            postgres.stop()
        }
    }

    @Test
    fun `lagrer IF_VEDFRIVT_10`() {
        lagreIfVedfrivt10(
            IF01_AGNR_FNR = 12345678901,
            IF10_VIRKDATO = 20260101,
            ID_VED = BigDecimal.ONE,
        )

        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf("SELECT COUNT(*) AS antall FROM IF_VEDFRIVT_10").map { it.int("antall") }.asSingle)
        }
        assertEquals(1, antall)
    }

    @Test
    fun `lagrer IF_FKONTO_12`() {
        lagreIfFkonto12(
            ID_KONT = BigDecimal.ONE,
        )

        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf("SELECT COUNT(*) AS antall FROM IF_FKONTO_12").map { it.int("antall") }.asSingle)
        }
        assertEquals(1, antall)
    }

    fun lagreIfVedfrivt10(
        IF01_KODE: Char = '1',
        IF01_AGNR_FNR: Long,
        IF10_FORSFOM_SEQ: Int = 0,
        IF10_GODKJ: Char = 'J',
        IF10_FORSFOM: Int = 0,
        IF10_VIRKDATO: Int,
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
        OPPRETTET: Instant = Instant.now(),
        ENDRET_I_KILDE: Instant = Instant.now(),
        KILDE_IF: String = " ",
        ID_VED: BigDecimal,
        OPPDATERT: Instant? = null,
    ) {
        dao.lagreIfVedfrivt10(
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

    fun lagreIfFkonto12(
        IF01_KODE: Char = '1',
        IF01_AGNR_FNR: Long? = null,
        IF10_FORSFOM_SEQ: Int? = null,
        IF12_BETDATO_SEQ: Int? = null,
        IF12_FOM: Int? = null,
        IF12_TOM: Int? = null,
        IF12_BET_KODE: Char? = null,
        IF12_FRIUKER: String? = null,
        IF12_BELOEP: BigDecimal? = null,
        IF12_BETDATO: Int? = null,
        OPPRETTET: Instant = Instant.now(),
        ENDRET_I_KILDE: Instant = Instant.now(),
        KILDE_IF: String = " ",
        ID_KONT: BigDecimal,
        OPPDATERT: Instant? = null,
    ) {
        dao.lagreIfFkonto12(
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
}
