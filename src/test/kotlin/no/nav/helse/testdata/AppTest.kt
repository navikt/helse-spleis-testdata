package no.nav.helse.testdata

import com.github.navikt.tbd_libs.naisful.test.TestContext
import com.github.navikt.tbd_libs.naisful.test.naisfulTestApp
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Accept
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.mockk.mockk
import no.nav.helse.testdata.api.registerInntektApi
import no.nav.helse.testdata.api.registerPersonApi
import no.nav.helse.testdata.api.registerVedtaksperiodeApi
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {
    companion object {
        private const val fnr1 = "123"
    }

    private val testRapid: TestRapid = TestRapid()
    private val rapidProducer =
        object : RapidProducer {
            override fun publish(message: String) {
                testRapid.publish(message)
            }

            override fun publish(
                key: String,
                message: String,
            ) {
                testRapid.publish(key, message)
            }
        }

    @BeforeEach
    fun beforeEach() {
        testRapid.reset()
    }

    @Test
    fun `slett person`() {
        e2e {
            val response =
                client.delete("/person") {
                    header("ident", fnr1)
                }

            assertTrue(response.status.isSuccess())
            assertEquals(1, testRapid.inspektør.size)
            assertEquals("slett_person", testRapid.inspektør.field(0, "@event_name").asString())
        }
    }

    @Test
    fun `opprett vedtak`() {
        e2e {
            val response =
                client.post("/vedtaksperiode") {
                    header("Content-Type", "application/json")
                    setBody(data())
                }
            assertTrue(response.status.isSuccess())
            val body = objectMapper.readTree(response.bodyAsText())
            assertTrue(body.has("traceUrl"))
            assertTrue(body.path("traceUrl").isNull, "Uten aktiv span skal det ikke lages trace-lenke")
        }
    }

    @Test
    fun `sender medlemskapsavklaring med årsaker`() {
        e2e {
            val response =
                client.post("/vedtaksperiode") {
                    header("Content-Type", "application/json")
                    setBody(
                        data(
                            medlemskap =
                                """
                                "medlemskapVerdi": "UAVKLART_MED_BRUKERSPORSMAAL",
                                "medlemskapAarsaker": [
                                    { "regelId": "REGEL_3", "forklaringPaaUavklart": "Ikke registrert arbeidsforhold" },
                                    { "regelId": "REGEL_23", "forklaringPaaUavklart": "Ikke opphold i Norge" }
                                ],
                                """,
                        ),
                    )
                }
            assertTrue(response.status.isSuccess())
            val avklaring = testRapid.inspektør.message(0)
            assertEquals("mock_medlemskap_avklaring", avklaring.path("@event_name").asText())
            assertEquals("UAVKLART_MED_BRUKERSPORSMAAL", avklaring.path("medlemskapVerdi").asText())
            assertEquals(listOf("REGEL_3", "REGEL_23"), avklaring.path("aarsaker").values().map { it.path("regelId").asText() })
            assertEquals("Ikke opphold i Norge", avklaring.path("aarsaker")[1].path("forklaringPaaUavklart").asText())
        }
    }

    @Test
    fun `sender ikke årsaker når medlemskapsavklaringen er JA`() {
        e2e {
            val response =
                client.post("/vedtaksperiode") {
                    header("Content-Type", "application/json")
                    setBody(
                        data(
                            medlemskap =
                                """
                                "medlemskapVerdi": "JA",
                                "medlemskapAarsaker": [{ "regelId": "REGEL_3", "forklaringPaaUavklart": "Skal ikke med" }],
                                """,
                        ),
                    )
                }
            assertTrue(response.status.isSuccess())
            val avklaring = testRapid.inspektør.message(0)
            assertEquals("JA", avklaring.path("medlemskapVerdi").asText())
            assertTrue(avklaring.path("aarsaker").isEmpty)
        }
    }

    @Test
    fun `sender ikke medlemskapsavklaring når medlemskapVerdi mangler`() {
        e2e {
            val response =
                client.post("/vedtaksperiode") {
                    header("Content-Type", "application/json")
                    setBody(data())
                }
            assertTrue(response.status.isSuccess())
            val eventnavn =
                (0 until testRapid.inspektør.size).map {
                    testRapid.inspektør
                        .message(it)
                        .path("@event_name")
                        .asText()
                }
            assertFalse("mock_medlemskap_avklaring" in eventnavn)
        }
    }

    @Test
    fun `sender beskjed om at medlemskap ikke skal mockes`() {
        e2e {
            val response =
                client.post("/vedtaksperiode") {
                    header("Content-Type", "application/json")
                    setBody(data(medlemskap = """"medlemskapVerdi": "IKKE_MOCK","""))
                }
            assertTrue(response.status.isSuccess())
            val avklaring = testRapid.inspektør.message(0)
            assertEquals("mock_medlemskap_avklaring", avklaring.path("@event_name").asText())
            assertFalse(avklaring.path("skalMockes").asBoolean())
            assertFalse(avklaring.has("medlemskapVerdi"))
        }
    }

    @Test
    fun `sender forhåndsvurdering før søknaden`() {
        e2e {
            val response =
                client.post("/vedtaksperiode") {
                    header("Content-Type", "application/json")
                    setBody(
                        data(
                            medlemskap =
                                """
                                "forhåndsvurdering": {
                                    "svar": "UAVKLART_MED_BRUKERSPORSMAAL",
                                    "aarsaker": [{ "regelId": "REGEL_3", "forklaringPaaUavklart": "Ikke registrert arbeidsforhold" }]
                                },
                                """,
                        ),
                    )
                }
            assertTrue(response.status.isSuccess())
            val meldinger = (0 until testRapid.inspektør.size).map { testRapid.inspektør.message(it) }
            val forhåndsvurderingIndeks = meldinger.indexOfFirst { it.has("speilSvar") }
            val søknadIndeks = meldinger.indexOfFirst { it.path("status").asText() == "SENDT" }
            assertTrue(forhåndsvurderingIndeks in 0 until søknadIndeks)

            val forhåndsvurdering = meldinger[forhåndsvurderingIndeks]
            assertFalse(forhåndsvurdering.has("@event_name"))
            assertEquals(meldinger[søknadIndeks].path("id").asText(), forhåndsvurdering.path("soknadId").asText())
            assertEquals("fnr", forhåndsvurdering.path("fnr").asText())
            assertEquals("UAVKLART_MED_BRUKERSPORSMAAL", forhåndsvurdering.path("speilSvar").asText())
            assertEquals("REGEL_3", forhåndsvurdering.path("aarsaker")[0].path("regelId").asText())
            assertEquals("2019-12-25", forhåndsvurdering.path("gyldigForSkjaeringstidspunktFom").asText())
            assertEquals("2020-01-26", forhåndsvurdering.path("gyldigForSkjaeringstidspunktTom").asText())
        }
    }

    @Test
    fun `sender ikke forhåndsvurdering uten søknad eller når den ikke er valgt`() {
        e2e {
            client.post("/vedtaksperiode") {
                header("Content-Type", "application/json")
                setBody(data(medlemskap = """"forhåndsvurdering": { "svar": "JA" },""", søknad = "null"))
            }
            client.post("/vedtaksperiode") {
                header("Content-Type", "application/json")
                setBody(data())
            }
            assertTrue((0 until testRapid.inspektør.size).none { testRapid.inspektør.message(it).has("speilSvar") })
        }
    }

    @Test
    fun `slå opp inntekt`() {
        e2e {
            val response =
                client.get("/person/inntekt") {
                    header(Accept, ContentType.Application.Json)
                    header("ident", "fnr")
                }
            assertTrue(response.status.isSuccess())
        }
    }

    private fun e2e(testblokk: suspend TestContext.() -> Unit) {
        naisfulTestApp(
            testApplicationModule = {
                routing {
                    registerPersonApi(RapidsMediator(rapidProducer), mockk())
                    registerInntektApi(inntektRestClient)
                    registerVedtaksperiodeApi(mediator = RapidsMediator(rapidProducer))
                }
            },
            objectMapper = objectMapper,
            meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT),
            testblokk = testblokk,
        )
    }

    @Language("json")
    private fun data(
        medlemskap: String = "",
        søknad: String = """{ "harAndreInntektskilder": true }""",
    ) =
        """
        {
            $medlemskap
            "fnr": "fnr",
            "orgnummer": "orgnummer",
            "sykdomFom": "2020-01-10",
            "sykdomTom": "2020-01-30",
            "arbeidssituasjon": "ARBEIDSTAKER",
            "søknad": $søknad,
            "inntektsmelding": ${inntektsmelding()}
        }
    """

    @Language("json")
    private fun inntektsmelding() =
        """
        {
            "inntekt": 0.0,
            "arbeidsgiverperiode": [
                { "fom": "2019-12-31", "tom": "2020-01-14" },
                { "fom": "2019-12-31", "tom": "2020-01-14" }
            ],
            "endringRefusjon": [],
            "refusjon": {},
            "førsteFraværsdag": "2019-12-31",
            "begrunnelseForReduksjonEllerIkkeUtbetalt": ""
        }
    """
}
