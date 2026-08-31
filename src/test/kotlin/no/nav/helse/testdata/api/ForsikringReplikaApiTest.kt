package no.nav.helse.testdata.api

import com.github.navikt.tbd_libs.naisful.test.TestContext
import com.github.navikt.tbd_libs.naisful.test.naisfulTestApp
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.math.BigDecimal
import java.time.Instant
import no.nav.helse.testdata.db.TestDatabase
import no.nav.helse.testdata.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ForsikringReplikaApiTest {
    private companion object {
        private const val VEDFRIVT_PATH = "/replikabase/if-vedfrivt-10"
        private const val FKONTO_PATH = "/replikabase/if-fkonto-12"
    }

    @Test
    fun `oppretter, henter, lister, oppdaterer og sletter IF_VEDFRIVT_10`() = e2e {
        val agnrFnr = 10000000001

        val opprettet = client.post(VEDFRIVT_PATH) { jsonBody(vedfrivt10(IF01_AGNR_FNR = agnrFnr)) }
        assertEquals(HttpStatusCode.Created, opprettet.status)
        val idVed = opprettet.json()["ID_VED"].decimalValue()

        val hentet = client.get("$VEDFRIVT_PATH/$idVed")
        assertEquals(HttpStatusCode.OK, hentet.status)
        assertEquals(agnrFnr, hentet.json()["IF01_AGNR_FNR"].asLong())

        val listet = client.get("$VEDFRIVT_PATH?IF01_AGNR_FNR=$agnrFnr")
        assertEquals(HttpStatusCode.OK, listet.status)
        assertEquals(listOf(idVed), listet.json().map { it["ID_VED"].decimalValue() })

        val oppdatert = client.put("$VEDFRIVT_PATH/$idVed") {
            jsonBody(vedfrivt10(IF01_AGNR_FNR = agnrFnr, IF10_PREMIE = 4321))
        }
        assertEquals(HttpStatusCode.OK, oppdatert.status)
        assertEquals(4321, oppdatert.json()["IF10_PREMIE"].asInt())
        assertEquals(idVed, oppdatert.json()["ID_VED"].decimalValue())

        assertEquals(HttpStatusCode.NoContent, client.delete("$VEDFRIVT_PATH/$idVed").status)
        assertEquals(HttpStatusCode.NotFound, client.get("$VEDFRIVT_PATH/$idVed").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("$VEDFRIVT_PATH/$idVed").status)
    }

    @Test
    fun `godtar ikke ID_VED ved oppretting`() = e2e {
        val response = client.post(VEDFRIVT_PATH) {
            jsonBody(vedfrivt10(IF01_AGNR_FNR = 10000000002, ID_VED = BigDecimal.ONE))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `krever at ID_VED i kroppen stemmer med URL-en`() = e2e {
        val idVed = client.post(VEDFRIVT_PATH) { jsonBody(vedfrivt10(IF01_AGNR_FNR = 10000000003)) }
            .json()["ID_VED"].decimalValue()

        val feil = client.put("$VEDFRIVT_PATH/$idVed") {
            jsonBody(vedfrivt10(IF01_AGNR_FNR = 10000000003, ID_VED = idVed.add(BigDecimal.ONE)))
        }
        assertEquals(HttpStatusCode.BadRequest, feil.status)

        val ok = client.put("$VEDFRIVT_PATH/$idVed") {
            jsonBody(vedfrivt10(IF01_AGNR_FNR = 10000000003, ID_VED = idVed))
        }
        assertEquals(HttpStatusCode.OK, ok.status)
    }

    @Test
    fun `oppretter, henter, lister, oppdaterer og sletter IF_FKONTO_12`() = e2e {
        val agnrFnr = 10000000004

        val opprettet = client.post(FKONTO_PATH) { jsonBody(fkonto12(IF01_AGNR_FNR = agnrFnr)) }
        assertEquals(HttpStatusCode.Created, opprettet.status)
        val idKont = opprettet.json()["ID_KONT"].decimalValue()

        assertEquals(HttpStatusCode.OK, client.get("$FKONTO_PATH/$idKont").status)

        val listet = client.get("$FKONTO_PATH?IF01_AGNR_FNR=$agnrFnr")
        assertEquals(listOf(idKont), listet.json().map { it["ID_KONT"].decimalValue() })
        assertTrue(client.get(FKONTO_PATH).json().any { it["ID_KONT"].decimalValue() == idKont })

        val oppdatert = client.put("$FKONTO_PATH/$idKont") {
            jsonBody(fkonto12(IF01_AGNR_FNR = agnrFnr, IF12_BELOEP = BigDecimal("1234.56")))
        }
        assertEquals(HttpStatusCode.OK, oppdatert.status)
        assertEquals(BigDecimal("1234.56"), oppdatert.json()["IF12_BELOEP"].decimalValue())

        assertEquals(HttpStatusCode.NoContent, client.delete("$FKONTO_PATH/$idKont").status)
        assertEquals(HttpStatusCode.NotFound, client.get("$FKONTO_PATH/$idKont").status)
    }

    @Test
    fun `godtar ikke ID_KONT ved oppretting`() = e2e {
        val response = client.post(FKONTO_PATH) {
            jsonBody(fkonto12(IF01_AGNR_FNR = 10000000005, ID_KONT = BigDecimal.ONE))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `svarer med 400 på ugyldig kropp`() = e2e {
        val response = client.post(VEDFRIVT_PATH) {
            header("Content-Type", "application/json")
            setBody("""{ "IF01_KODE": "1" }""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private fun vedfrivt10(
        IF01_AGNR_FNR: Long,
        IF10_PREMIE: Int = 0,
        ID_VED: BigDecimal? = null,
    ) = IfVedfrivt10Request(
        IF01_KODE = '1',
        IF01_AGNR_FNR = IF01_AGNR_FNR,
        IF10_FORSFOM_SEQ = 0,
        IF10_GODKJ = 'J',
        IF10_FORSFOM = 0,
        IF10_VIRKDATO = 20260101,
        IF10_TYPE = '1',
        IF10_SELVFOM = " ",
        IF10_KOMBI = ' ',
        IF10_PREMGRL = 0,
        IF10_FOM = 0,
        IF10_PREMIE = IF10_PREMIE,
        IF10_GML_PREMGRL = 0,
        IF10_GML_FOM = 0,
        IF10_GML_PREMIE = 0,
        IF10_FRIFOM = 0,
        IF10_FORSTOM = 0,
        IF10_OPPHGR = " ",
        IF10_VARSEL = 0,
        IF10_TERM_KV = ' ',
        IF10_TERM_AAR = " ",
        IF10_VARSEL_BELOEP = 0,
        IF10_BETALT_BELOEP = 0,
        IF10_PURR = 0,
        IF10_TKNR_BOST = 0,
        IF10_TKNR_BEH = 0,
        OPPRETTET = Instant.parse("2026-01-01T00:00:00Z"),
        ENDRET_I_KILDE = Instant.parse("2026-01-01T00:00:00Z"),
        KILDE_IF = " ",
        OPPDATERT = null,
        ID_VED = ID_VED,
    )

    private fun fkonto12(
        IF01_AGNR_FNR: Long,
        IF12_BELOEP: BigDecimal? = null,
        ID_KONT: BigDecimal? = null,
    ) = IfFkonto12Request(
        IF01_KODE = '1',
        IF01_AGNR_FNR = IF01_AGNR_FNR,
        IF10_FORSFOM_SEQ = null,
        IF12_BETDATO_SEQ = null,
        IF12_FOM = null,
        IF12_TOM = null,
        IF12_BET_KODE = null,
        IF12_FRIUKER = null,
        IF12_BELOEP = IF12_BELOEP,
        IF12_BETDATO = null,
        OPPRETTET = Instant.parse("2026-01-01T00:00:00Z"),
        ENDRET_I_KILDE = Instant.parse("2026-01-01T00:00:00Z"),
        KILDE_IF = " ",
        OPPDATERT = null,
        ID_KONT = ID_KONT,
    )

    private fun HttpRequestBuilder.jsonBody(body: Any) {
        header("Content-Type", "application/json")
        setBody(objectMapper.writeValueAsString(body))
    }

    private suspend fun HttpResponse.json() = objectMapper.readTree(bodyAsText())

    private fun e2e(testblokk: suspend TestContext.() -> Unit) {
        naisfulTestApp(
            testApplicationModule = {
                routing {
                    registerForsikringReplikaApi(TestDatabase.dao)
                }
            },
            objectMapper = objectMapper,
            meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT),
            testblokk = testblokk
        )
    }
}
