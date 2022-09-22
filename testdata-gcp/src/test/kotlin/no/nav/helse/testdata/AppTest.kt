package no.nav.helse.testdata

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.testdata.api.registerVedtaksperiodeApi
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    private val testRapid: TestRapid = TestRapid()

    @BeforeEach
    fun beforeEach() {
        testRapid.reset()
    }

    @Test
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
                setBody(data())
            }
            assertTrue(response.status.isSuccess())
        }
    }

    @Language("JSON")
    private fun data() = """
        {
            "fnr": "fnr",
            "orgnummer": "orgnummer",
            "sykdomFom": "2020-01-10",
            "sykdomTom": "2020-01-30",
            "søknad": { "harAndreInntektskilder": true },
            "inntektsmelding": ${inntektsmelding()}
        }
    """

    @Language("JSON")
    private fun inntektsmelding() = """
        {
            "inntekt": 0.0,
            "ferieperioder": [],
            "arbeidsgiverperiode": [
                { "fom": "2019-12-31", "tom": "2020-01-14" },
                { "fom": "2019-12-31", "tom": "2020-01-14" }
            ],
            "endringRefusjon": [],
            "refusjon": {},
            "førsteFraværsdag": "2019-12-31"
        }
    """
}
