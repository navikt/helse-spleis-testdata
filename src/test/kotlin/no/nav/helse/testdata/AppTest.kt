package no.nav.helse.testdata

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.testdata.api.registerPersonApi
import no.nav.helse.testdata.api.registerVedtaksperiodeApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    companion object {
        private const val fnr1 = "123"
    }

    private lateinit var testRapid: TestRapid

    @BeforeEach
    fun beforeEach() {
        testRapid = TestRapid()
    }

    @Test
    fun `slett person`() {
        testApplication {
            application {
                routing {
                    registerPersonApi(RapidsMediator(testRapid))
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
    @Disabled
    fun `opprett vedtak`() {
        testApplication {
            application {
                routing {
                    registerVedtaksperiodeApi(mediator = RapidsMediator(testRapid))
                }
                installJacksonFeature()
            }
            val response = client.post("/vedtaksperiode") {
                header("Content-Type", "application/json")
                setBody(
                    """
                    {
                        "fnr": "fnr",
                        "orgnummer": "orgnummer",
                        "sykdomFom": "2020-01-10",
                        "sykdomTom": "2020-01-30",
                        "inntekt": 0.0,
                        "harAndreInntektskilder": true,
                        "skalSendeInntektsmelding": true,
                        "førstefraværsdag": "2019-12-31",
                        "arbeidsgiverperiode": [{"fom": "2019-12-31", "tom": "2020-01-14"}, {"fom": "2019-12-31", "tom": "2020-01-14"}],
                        "ferieperioder": []
                    }
                    """
                )
            }
            assertTrue(response.status.isSuccess())
        }
    }
}
