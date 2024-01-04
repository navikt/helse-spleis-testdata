package no.nav.helse.testdata

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Accept
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.testdata.api.registerInntektApi
import no.nav.helse.testdata.api.registerPersonApi
import no.nav.helse.testdata.api.registerVedtaksperiodeApi
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
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

    @BeforeEach
    fun beforeEach() {
        testRapid.reset()
    }

    @Test
    fun `slett person`() {
        testApplication {
            application {
                routing {
                    registerPersonApi(RapidsMediator(testRapid), mockk())
                }
            }
            val response = client.delete("/person") {
                header("ident", fnr1)
            }

            assertTrue(response.status.isSuccess())
            assertEquals(1, testRapid.inspektør.size)
            assertEquals("slett_person", testRapid.inspektør.field(0, "@event_name").asText())
        }
    }

    @Test
    fun `opprett vedtak`() {
        testApplication {
            application {
                routing {
                    registerVedtaksperiodeApi(
                        mediator = RapidsMediator(testRapid)
                    )
                }
                installJacksonFeature()
            }
            val response = client.post("/vedtaksperiode") {
                header("Content-Type", "application/json")
                setBody(data())
            }
            assertTrue(response.status.isSuccess())
        }
    }

    @Test
    fun `slå opp inntekt`() {
        testApplication {
            application {
                installJacksonFeature()
                routing {
                    registerInntektApi(inntektRestClient)
                }
            }
            val response = client.get("/person/inntekt") {
                header(Accept, ContentType.Application.Json)
                header("ident", "fnr")
            }
            assertTrue(response.status.isSuccess())
        }
    }

    @Language("json")
    private fun data() = """
        {
            "fnr": "fnr",
            "orgnummer": "orgnummer",
            "sykdomFom": "2020-01-10",
            "sykdomTom": "2020-01-30",
            "søknad": { "harAndreInntektskilder": true, "arbeidssituasjon": "ARBEIDSTAKER" },
            "inntektsmelding": ${inntektsmelding()}
        }
    """

    @Language("json")
    private fun inntektsmelding() = """
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
