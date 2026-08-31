package no.nav.helse.testdata.db

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ForsikringReplikaTestdataDaoTest {
    private val dao = TestDatabase.dao

    @Test
    fun `lagrer og henter IF_VEDFRIVT_10`() {
        val rad = ifVedfrivt10(IF01_AGNR_FNR = 12345678901, ID_VED = dao.nesteIdVed())
        dao.lagreIfVedfrivt10(rad)

        assertEquals(rad, dao.hentIfVedfrivt10(rad.ID_VED))
    }

    @Test
    fun `lister IF_VEDFRIVT_10 på IF01_AGNR_FNR`() {
        val agnrFnr = 22222222222
        val første = ifVedfrivt10(IF01_AGNR_FNR = agnrFnr, ID_VED = dao.nesteIdVed())
        val andre = ifVedfrivt10(IF01_AGNR_FNR = agnrFnr, ID_VED = dao.nesteIdVed())
        val annenPerson = ifVedfrivt10(IF01_AGNR_FNR = 33333333333, ID_VED = dao.nesteIdVed())
        listOf(første, andre, annenPerson).forEach(dao::lagreIfVedfrivt10)

        assertEquals(listOf(første, andre), dao.finnIfVedfrivt10(agnrFnr))
    }

    @Test
    fun `oppdaterer og sletter IF_VEDFRIVT_10`() {
        val rad = ifVedfrivt10(IF01_AGNR_FNR = 44444444444, ID_VED = dao.nesteIdVed())
        dao.lagreIfVedfrivt10(rad)

        val endret = rad.copy(IF10_PREMIE = 1234, KILDE_IF = "ENDRET")
        assertTrue(dao.oppdaterIfVedfrivt10(endret))
        assertEquals(endret, dao.hentIfVedfrivt10(rad.ID_VED))

        assertTrue(dao.slettIfVedfrivt10(rad.ID_VED))
        assertNull(dao.hentIfVedfrivt10(rad.ID_VED))
        assertFalse(dao.slettIfVedfrivt10(rad.ID_VED))
    }

    @Test
    fun `oppdaterer ikke ukjent IF_VEDFRIVT_10`() {
        val ukjent = ifVedfrivt10(IF01_AGNR_FNR = 55555555555, ID_VED = BigDecimal("-1"))
        assertFalse(dao.oppdaterIfVedfrivt10(ukjent))
    }

    @Test
    fun `lagrer og henter IF_FKONTO_12`() {
        val rad = ifFkonto12(
            IF01_AGNR_FNR = 12345678901,
            IF12_BELOEP = BigDecimal("1234.56"),
            ID_KONT = dao.nesteIdKont(),
        )
        dao.lagreIfFkonto12(rad)

        assertEquals(rad, dao.hentIfFkonto12(rad.ID_KONT))
    }

    @Test
    fun `lister IF_FKONTO_12 på IF01_AGNR_FNR`() {
        val agnrFnr = 66666666666
        val rad = ifFkonto12(IF01_AGNR_FNR = agnrFnr, ID_KONT = dao.nesteIdKont())
        dao.lagreIfFkonto12(rad)
        dao.lagreIfFkonto12(ifFkonto12(IF01_AGNR_FNR = 77777777777, ID_KONT = dao.nesteIdKont()))

        assertEquals(listOf(rad), dao.finnIfFkonto12(agnrFnr))
        assertTrue(dao.finnIfFkonto12(null).contains(rad))
    }

    @Test
    fun `oppdaterer og sletter IF_FKONTO_12`() {
        val rad = ifFkonto12(IF01_AGNR_FNR = 88888888888, ID_KONT = dao.nesteIdKont())
        dao.lagreIfFkonto12(rad)

        val endret = rad.copy(IF12_FOM = 20260101, IF12_TOM = 20260131)
        assertTrue(dao.oppdaterIfFkonto12(endret))
        assertEquals(endret, dao.hentIfFkonto12(rad.ID_KONT))

        assertTrue(dao.slettIfFkonto12(rad.ID_KONT))
        assertNull(dao.hentIfFkonto12(rad.ID_KONT))
        assertFalse(dao.slettIfFkonto12(rad.ID_KONT))
    }
}
