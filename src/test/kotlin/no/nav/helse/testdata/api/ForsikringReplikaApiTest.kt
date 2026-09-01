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
import java.time.LocalDate
import no.nav.helse.testdata.db.TestDatabase
import no.nav.helse.testdata.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ForsikringReplikaApiTest {
    private companion object {
        private const val VEDFRIVT_PATH = "/replikabase/if-vedfrivt-10"
        private const val FKONTO_PATH = "/replikabase/if-fkonto-12"
        private const val FORSIKRING_PATH = "/individuelle-forsikringer"
        private const val FAKTURA_PATH = "/forsikringsfakturaer"
        private fun forsikringerFor(identitetsnummer: String) =
            "/personer/$identitetsnummer/individuelle-forsikringer"
        private fun fakturaerFor(ID_VED: BigDecimal) = "$FORSIKRING_PATH/$ID_VED/forsikringsfakturaer"
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

    @Test
    fun `oppretter forenklet individuell forsikring`() = e2e {
        val response = client.post(forsikringerFor("31129012345")) {
            jsonBody(
                individuellForsikring(
                    godkjent = true,
                    fom = LocalDate.parse("2026-01-01"),
                    virkningsdato = LocalDate.parse("2026-02-01"),
                    type = IndividuellForsikringType.SELVSTENDIG_JORDBRUKER_100_PROSENT_FRA_DAG_1,
                    premiegrunnlag = 500000,
                    opphørsdato = LocalDate.parse("2026-12-31"),
                    opphørsgrunn = "5",
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)

        val rad = client.get("$VEDFRIVT_PATH/${response.json()["id"].decimalValue()}").json()
        assertEquals("1", rad["IF01_KODE"].asText())
        assertEquals(90123112345, rad["IF01_AGNR_FNR"].asLong())
        assertEquals(rad["ID_VED"].asInt(), rad["IF10_FORSFOM_SEQ"].asInt())
        assertEquals("J", rad["IF10_GODKJ"].asText())
        assertEquals(20260101, rad["IF10_FORSFOM"].asInt())
        assertEquals(20260201, rad["IF10_VIRKDATO"].asInt())
        assertEquals("4", rad["IF10_TYPE"].asText())
        assertEquals(" ", rad["IF10_SELVFOM"].asText())
        assertEquals("N", rad["IF10_KOMBI"].asText())
        assertEquals(500000, rad["IF10_PREMGRL"].asInt())
        assertEquals(20261231, rad["IF10_FORSTOM"].asInt())
        assertEquals("5", rad["IF10_OPPHGR"].asText())
        assertEquals(" ", rad["IF10_TERM_KV"].asText())
        assertEquals(" ", rad["IF10_TERM_AAR"].asText())
        assertEquals(" ", rad["KILDE_IF"].asText())
        assertEquals(rad["OPPRETTET"].asText(), rad["ENDRET_I_KILDE"].asText())
        assertEquals(rad["OPPRETTET"].asText(), rad["OPPDATERT"].asText())
    }

    @Test
    fun `bruker nulleverdier når forsikringen ikke er opphørt`() = e2e {
        val opprettet = client.post(forsikringerFor("01015012345")) {
            jsonBody(
                individuellForsikring(
                    godkjent = false,
                    fom = LocalDate.parse("2026-01-01"),
                    virkningsdato = LocalDate.parse("2026-01-01"),
                    type = IndividuellForsikringType.SELVSTENDIG_80_PROSENT_FRA_DAG_1,
                    premiegrunnlag = 0,
                    opphørsdato = null,
                    opphørsgrunn = null,
                )
            )
        }.json()

        val rad = client.get("$VEDFRIVT_PATH/${opprettet["id"].decimalValue()}").json()
        assertEquals(50010112345, rad["IF01_AGNR_FNR"].asLong())
        assertEquals("N", rad["IF10_GODKJ"].asText())
        assertEquals("1", rad["IF10_TYPE"].asText())
        assertEquals(0, rad["IF10_FORSTOM"].asInt())
        assertEquals(" ", rad["IF10_OPPHGR"].asText())
    }

    @Test
    fun `avviser ugyldig identitetsnummer`() = e2e {
        val response = client.post(forsikringerFor("123")) { jsonBody(individuellForsikring()) }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `oppdaterer forenklet individuell forsikring`() = e2e {
        val idVed = client.post(forsikringerFor("31129012345")) {
            jsonBody(individuellForsikring())
        }.json()["id"].decimalValue()
        val lagret = client.get("$VEDFRIVT_PATH/$idVed").json()

        val oppdatert = client.put("$FORSIKRING_PATH/$idVed") {
            jsonBody(
                individuellForsikring(
                    godkjent = false,
                    type = IndividuellForsikringType.FRILANSER_100_PROSENT_FRA_DAG_1,
                    premiegrunnlag = 123456,
                    opphørsdato = LocalDate.parse("2026-12-31"),
                    opphørsgrunn = "5",
                )
            )
        }
        assertEquals(HttpStatusCode.OK, oppdatert.status)

        val forsikring = oppdatert.json()
        assertEquals(idVed, forsikring["id"].decimalValue())
        assertEquals("31129012345", forsikring["identitetsnummer"].asText())
        assertEquals(false, forsikring["godkjent"].asBoolean())
        assertEquals("FRILANSER_100_PROSENT_FRA_DAG_1", forsikring["type"].asText())
        assertEquals(123456, forsikring["premiegrunnlag"].asInt())
        assertEquals("2026-12-31", forsikring["opphørsdato"].asText())
        assertEquals("5", forsikring["opphørsgrunn"].asText())

        val rad = client.get("$VEDFRIVT_PATH/$idVed").json()
        assertEquals(lagret["OPPRETTET"].asText(), rad["OPPRETTET"].asText())
        assertNotEquals(lagret["OPPDATERT"].asText(), rad["OPPDATERT"].asText())
    }

    @Test
    fun `svarer med 404 når forsikringen som oppdateres ikke finnes`() = e2e {
        val response = client.put("$FORSIKRING_PATH/-1") { jsonBody(individuellForsikring()) }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `henter forenklet individuell forsikring`() = e2e {
        val idVed = client.post(forsikringerFor("01015012345")) {
            jsonBody(
                individuellForsikring(
                    godkjent = false,
                    fom = LocalDate.parse("2026-03-01"),
                    virkningsdato = LocalDate.parse("2026-04-01"),
                    type = IndividuellForsikringType.SELVSTENDIG_100_PROSENT_FRA_DAG_17,
                    premiegrunnlag = 654321,
                    opphørsdato = LocalDate.parse("2026-12-31"),
                    opphørsgrunn = "5",
                )
            )
        }.json()["id"].decimalValue()

        val hentet = client.get("$FORSIKRING_PATH/$idVed")
        assertEquals(HttpStatusCode.OK, hentet.status)

        val forsikring = hentet.json()
        assertEquals(idVed, forsikring["id"].decimalValue())
        assertEquals("01015012345", forsikring["identitetsnummer"].asText())
        assertEquals(false, forsikring["godkjent"].asBoolean())
        assertEquals("2026-03-01", forsikring["fom"].asText())
        assertEquals("2026-04-01", forsikring["virkningsdato"].asText())
        assertEquals("SELVSTENDIG_100_PROSENT_FRA_DAG_17", forsikring["type"].asText())
        assertEquals(654321, forsikring["premiegrunnlag"].asInt())
        assertEquals("2026-12-31", forsikring["opphørsdato"].asText())
        assertEquals("5", forsikring["opphørsgrunn"].asText())

        assertEquals(HttpStatusCode.NoContent, client.delete("$FORSIKRING_PATH/$idVed").status)
        assertEquals(HttpStatusCode.NotFound, client.get("$FORSIKRING_PATH/$idVed").status)
    }

    @Test
    fun `tolker manglende verdier som null`() = e2e {
        val idVed = client.post(forsikringerFor("31129012345")) {
            jsonBody(individuellForsikring(opphørsdato = null, opphørsgrunn = null))
        }.json()["id"].decimalValue()

        val forsikring = client.get("$FORSIKRING_PATH/$idVed").json()
        assertTrue(forsikring["opphørsdato"].isNull)
        assertTrue(forsikring["opphørsgrunn"].isNull)
    }

    @Test
    fun `lister individuelle forsikringer sortert på virkningsdato og opphørsdato`() = e2e {
        val identitetsnummer = "17108012345"
        suspend fun opprett(virkningsdato: String, opphørsdato: String?) =
            client.post(forsikringerFor(identitetsnummer)) {
                jsonBody(
                    individuellForsikring(
                        virkningsdato = LocalDate.parse(virkningsdato),
                        opphørsdato = opphørsdato?.let(LocalDate::parse),
                    )
                )
            }.json()["id"].decimalValue()

        val utenOpphør = opprett("2026-02-01", null)
        val sist = opprett("2026-03-01", "2026-06-30")
        val først = opprett("2026-02-01", "2026-06-30")
        val nestSist = opprett("2026-02-01", "2026-12-31")

        val listet = client.get(forsikringerFor(identitetsnummer))
        assertEquals(HttpStatusCode.OK, listet.status)
        assertEquals(
            listOf(først, nestSist, utenOpphør, sist),
            listet.json().map { it["id"].decimalValue() }
        )
    }

    @Test
    fun `avviser ugyldig identitetsnummer ved listing`() = e2e {
        assertEquals(HttpStatusCode.BadRequest, client.get(forsikringerFor("123")).status)
    }

    @Test
    fun `sletter forenklet individuell forsikring`() = e2e {
        val idVed = client.post(forsikringerFor("31129012345")) { jsonBody(individuellForsikring()) }
            .json()["id"].decimalValue()

        assertEquals(HttpStatusCode.NoContent, client.delete("$FORSIKRING_PATH/$idVed").status)
        assertEquals(HttpStatusCode.NotFound, client.get("$VEDFRIVT_PATH/$idVed").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("$FORSIKRING_PATH/$idVed").status)
    }

    @Test
    fun `oppretter, lister, henter, oppdaterer og sletter forsikringsfakturaer`() = e2e {
        val idVed = client.post(forsikringerFor("31129012345")) { jsonBody(individuellForsikring()) }
            .json()["id"].decimalValue()

        val opprettet = client.post(fakturaerFor(idVed)) {
            jsonBody(forsikringsfaktura(år = 2026, halvdel = 2, betalingsdato = LocalDate.parse("2026-08-15")))
        }
        assertEquals(HttpStatusCode.Created, opprettet.status)
        val idKont = opprettet.json()["id"].decimalValue()
        assertEquals(2026, opprettet.json()["år"].asInt())
        assertEquals(2, opprettet.json()["halvdel"].asInt())
        assertEquals("2026-08-15", opprettet.json()["betalingsdato"].asText())

        val forsikring = client.get("$VEDFRIVT_PATH/$idVed").json()
        val rad = client.get("$FKONTO_PATH/$idKont").json()
        assertEquals(forsikring["IF01_KODE"].asText(), rad["IF01_KODE"].asText())
        assertEquals(forsikring["IF01_AGNR_FNR"].asLong(), rad["IF01_AGNR_FNR"].asLong())
        assertEquals(forsikring["IF10_FORSFOM_SEQ"].asInt(), rad["IF10_FORSFOM_SEQ"].asInt())
        assertEquals(idKont.toInt(), rad["IF12_BETDATO_SEQ"].asInt())
        assertEquals(20260701, rad["IF12_FOM"].asInt())
        assertEquals(20261231, rad["IF12_TOM"].asInt())
        assertEquals("B", rad["IF12_BET_KODE"].asText())
        assertTrue(rad["IF12_FRIUKER"].isNull)
        assertTrue(rad["IF12_BELOEP"].isNull)
        assertEquals(20260815, rad["IF12_BETDATO"].asInt())
        assertEquals(" ", rad["KILDE_IF"].asText())
        assertEquals(rad["OPPRETTET"].asText(), rad["ENDRET_I_KILDE"].asText())
        assertEquals(rad["OPPRETTET"].asText(), rad["OPPDATERT"].asText())

        val oppdatert = client.put("$FAKTURA_PATH/$idKont") {
            jsonBody(forsikringsfaktura(år = 2025, halvdel = 1, betalingsdato = null))
        }
        assertEquals(HttpStatusCode.OK, oppdatert.status)
        assertEquals(2025, oppdatert.json()["år"].asInt())
        assertEquals(1, oppdatert.json()["halvdel"].asInt())
        assertTrue(oppdatert.json()["betalingsdato"].isNull)

        val oppdatertRad = client.get("$FKONTO_PATH/$idKont").json()
        assertEquals(20250101, oppdatertRad["IF12_FOM"].asInt())
        assertEquals(20250630, oppdatertRad["IF12_TOM"].asInt())
        assertEquals(" ", oppdatertRad["IF12_BET_KODE"].asText())
        assertTrue(oppdatertRad["IF12_BETDATO"].isNull)
        assertEquals(forsikring["IF01_AGNR_FNR"].asLong(), oppdatertRad["IF01_AGNR_FNR"].asLong())
        assertEquals(rad["OPPRETTET"].asText(), oppdatertRad["OPPRETTET"].asText())
        assertNotEquals(rad["OPPDATERT"].asText(), oppdatertRad["OPPDATERT"].asText())

        assertEquals(HttpStatusCode.OK, client.get("$FAKTURA_PATH/$idKont").status)
        assertEquals(HttpStatusCode.NoContent, client.delete("$FAKTURA_PATH/$idKont").status)
        assertEquals(HttpStatusCode.NotFound, client.get("$FAKTURA_PATH/$idKont").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("$FAKTURA_PATH/$idKont").status)
    }

    @Test
    fun `lister bare fakturaene som hører til forsikringen`() = e2e {
        val identitetsnummer = "17108012345"
        val første = client.post(forsikringerFor(identitetsnummer)) { jsonBody(individuellForsikring()) }
            .json()["id"].decimalValue()
        val andre = client.post(forsikringerFor(identitetsnummer)) { jsonBody(individuellForsikring()) }
            .json()["id"].decimalValue()

        val høst = client.post(fakturaerFor(første)) { jsonBody(forsikringsfaktura(år = 2026, halvdel = 2)) }
            .json()["id"].decimalValue()
        val vår = client.post(fakturaerFor(første)) { jsonBody(forsikringsfaktura(år = 2026, halvdel = 1)) }
            .json()["id"].decimalValue()
        val annenForsikring = client.post(fakturaerFor(andre)) { jsonBody(forsikringsfaktura(år = 2026, halvdel = 1)) }
            .json()["id"].decimalValue()

        val listet = client.get(fakturaerFor(første))
        assertEquals(HttpStatusCode.OK, listet.status)
        assertEquals(listOf(vår, høst), listet.json().map { it["id"].decimalValue() })
        assertEquals(listOf(annenForsikring), client.get(fakturaerFor(andre)).json().map { it["id"].decimalValue() })
    }

    @Test
    fun `avviser ugyldig halvår`() = e2e {
        val idVed = client.post(forsikringerFor("31129012345")) { jsonBody(individuellForsikring()) }
            .json()["id"].decimalValue()
        val idKont = client.post(fakturaerFor(idVed)) { jsonBody(forsikringsfaktura()) }
            .json()["id"].decimalValue()

        assertEquals(
            HttpStatusCode.BadRequest,
            client.post(fakturaerFor(idVed)) { jsonBody(forsikringsfaktura(halvdel = 3)) }.status
        )
        assertEquals(
            HttpStatusCode.BadRequest,
            client.post(fakturaerFor(idVed)) { jsonBody(forsikringsfaktura(år = 26)) }.status
        )
        assertEquals(
            HttpStatusCode.BadRequest,
            client.put("$FAKTURA_PATH/$idKont") { jsonBody(forsikringsfaktura(halvdel = 0)) }.status
        )
    }

    @Test
    fun `svarer med 404 for fakturaer på en forsikring som ikke finnes`() = e2e {
        assertEquals(HttpStatusCode.NotFound, client.get(fakturaerFor(BigDecimal("-1"))).status)
        assertEquals(
            HttpStatusCode.NotFound,
            client.post(fakturaerFor(BigDecimal("-1"))) { jsonBody(forsikringsfaktura()) }.status
        )
        assertEquals(HttpStatusCode.NotFound, client.get("$FAKTURA_PATH/-1").status)
        assertEquals(HttpStatusCode.NotFound, client.put("$FAKTURA_PATH/-1") { jsonBody(forsikringsfaktura()) }.status)
    }

    private fun forsikringsfaktura(
        år: Int = 2026,
        halvdel: Int = 1,
        betalingsdato: LocalDate? = null,
    ) = ForsikringsfakturaRequest(år = år, halvdel = halvdel, betalingsdato = betalingsdato)

    private fun individuellForsikring(
        godkjent: Boolean = true,
        fom: LocalDate = LocalDate.parse("2026-01-01"),
        virkningsdato: LocalDate = LocalDate.parse("2026-02-01"),
        type: IndividuellForsikringType = IndividuellForsikringType.SELVSTENDIG_JORDBRUKER_100_PROSENT_FRA_DAG_1,
        premiegrunnlag: Int = 500000,
        opphørsdato: LocalDate? = null,
        opphørsgrunn: String? = null,
    ) = IndividuellForsikringRequest(
        godkjent = godkjent,
        fom = fom,
        virkningsdato = virkningsdato,
        type = type,
        premiegrunnlag = premiegrunnlag,
        opphørsdato = opphørsdato,
        opphørsgrunn = opphørsgrunn,
    )

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
