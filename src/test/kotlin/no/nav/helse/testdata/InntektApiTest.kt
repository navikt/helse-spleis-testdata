package no.nav.helse.testdata

import com.github.navikt.tbd_libs.naisful.test.TestContext
import com.github.navikt.tbd_libs.naisful.test.naisfulTestApp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import no.nav.helse.testdata.api.registerInntektApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.YearMonth

class InntektApiTest {
    private val inntektRestClient = mockk<InntektRestClient>()

    @ParameterizedTest
    @CsvSource(
        "2025-01-01, 2024-01, 2024-12",
        "2025-01-15, 2024-01, 2024-12",
        "2025-01-31, 2024-01, 2024-12",
        "2024-02-29, 2023-02, 2024-01",
        "2026-12-15, 2025-12, 2026-11",
    )
    fun `henter tolv hele måneder før første fraværsdag`(
        førsteFraværsdag: String,
        fom: String,
        tom: String,
    ) {
        coEvery { inntektRestClient.hentInntektsliste(any(), any(), any(), any(), any()) } returns Result.Ok(emptyList())

        e2e {
            val response =
                client.get("/person/inntekt?foersteFravaersdag=$førsteFraværsdag") {
                    header("ident", "fnr")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            coVerify(exactly = 1) {
                inntektRestClient.hentInntektsliste("fnr", YearMonth.parse(fom), YearMonth.parse(tom), "8-28", any())
            }
        }
    }

    @Test
    fun `beholder dagens dato som utgangspunkt når første fraværsdag ikke er angitt`() {
        coEvery { inntektRestClient.hentInntektsliste(any(), any(), any(), any(), any()) } returns Result.Ok(emptyList())

        e2e {
            val response =
                client.get("/person/inntekt") {
                    header("ident", "fnr")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            coVerify(exactly = 1) {
                inntektRestClient.hentInntektsliste("fnr", YearMonth.now().minusMonths(12), YearMonth.now().minusMonths(1), "8-28", any())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "ugyldig", "2025-02-30", "15.01.2025"])
    fun `avviser ugyldig første fraværsdag`(førsteFraværsdag: String) {
        e2e {
            val response =
                client.get("/person/inntekt?foersteFravaersdag=$førsteFraværsdag") {
                    header("ident", "fnr")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            coVerify(exactly = 0) { inntektRestClient.hentInntektsliste(any(), any(), any(), any(), any()) }
        }
    }

    @Test
    fun `summerer per arbeidsgiver og deler fortsatt på tolv`() {
        coEvery { inntektRestClient.hentInntektsliste(any(), any(), any(), any(), any()) } returns
            Result.Ok(
                listOf(
                    Måned(
                        YearMonth.of(2024, 12),
                        listOf(
                            Inntekt(30000.0, Inntektstype.LOENNSINNTEKT, "123456789"),
                            Inntekt(1000.0, Inntektstype.LOENNSINNTEKT, "123456789"),
                            Inntekt(60000.0, Inntektstype.LOENNSINNTEKT, "987654321"),
                        ),
                    ),
                ),
            )

        e2e {
            val response =
                client.get("/person/inntekt?foersteFravaersdag=2025-01-15") {
                    header("ident", "fnr")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val arbeidsgivere = objectMapper.readTree(response.bodyAsText()).path("arbeidsgivere")
            assertEquals(2, arbeidsgivere.size())
            assertEquals("123456789", arbeidsgivere[0].path("organisasjonsnummer").asString())
            assertEquals(2583.33, arbeidsgivere[0].path("beregnetMånedsinntekt").asDouble())
            assertEquals("987654321", arbeidsgivere[1].path("organisasjonsnummer").asString())
            assertEquals(5000.0, arbeidsgivere[1].path("beregnetMånedsinntekt").asDouble())
        }
    }

    private fun e2e(testblokk: suspend TestContext.() -> Unit) {
        naisfulTestApp(
            testApplicationModule = {
                routing {
                    registerInntektApi(inntektRestClient)
                }
            },
            objectMapper = objectMapper,
            meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT),
            testblokk = testblokk,
        )
    }
}
