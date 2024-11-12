package no.nav.helse.testdata

import com.github.navikt.tbd_libs.naisful.test.TestContext
import com.github.navikt.tbd_libs.naisful.test.naisfulTestApp
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.ktor.client.request.*
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
    private val rapidProducer = object : RapidProducer {
        override fun publish(message: String) {
            testRapid.publish(message)
        }

        override fun publish(key: String, message: String) {
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
        e2e {
            val response = client.post("/vedtaksperiode") {
                header("Content-Type", "application/json")
                setBody(data())
            }
            assertTrue(response.status.isSuccess())
        }
    }

    @Test
    fun `slå opp inntekt`() {
        e2e {
            val response = client.get("/person/inntekt") {
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
            testblokk = testblokk
        )
    }

    @Language("json")
    private fun data() = """
        {
            "fnr": "fnr",
            "orgnummer": "orgnummer",
            "sykdomFom": "2020-01-10",
            "sykdomTom": "2020-01-30",
            "arbeidssituasjon": "ARBEIDSTAKER",
            "søknad": { "harAndreInntektskilder": true },
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
