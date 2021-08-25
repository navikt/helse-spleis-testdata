package no.nav.helse.testdata

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.fullPath
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.testdata.Result.Ok
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.YearMonth

class InntektRestClientTest {

    private val responsMock = mockk<Mock>()

    @Test
    fun `person uten inntektshistorikk`() = runBlocking {
        responsMock.apply { every { get() }.returns(tomRespons()) }
        val inntektsliste =
            inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")

        when (inntektsliste) {
            is Ok -> {
                assertNotNull(inntektsliste.value)
                assertEquals(0, inntektsliste.value.size)
            }
            else -> fail()
        }
    }

    @Test
    fun `person med inntektshistorikk`() = runBlocking {
        responsMock.apply { every { get() }.returns(responsMedInntekt()) }
        val inntektsliste =
            inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")
        when (inntektsliste) {
            is Ok -> {
                assertNotNull(inntektsliste.value)
                assertEquals(1, inntektsliste.value.size)
            }
            else -> fail()
        }
    }



    private val inntektRestClient = InntektRestClient(
        "http://localhost.no", HttpClient(MockEngine) {
            install(JsonFeature) {
                this.serializer = JacksonSerializer() {
                    registerModule(JavaTimeModule())
                }
            }
            engine {
                addHandler { request ->
                    if (request.url.fullPath.startsWith("/api/v1/hentinntektliste")) {
                        respond(responsMock.get())
                    } else {
                        error("Endepunktet finnes ikke ${request.url.fullPath}")
                    }
                }
            }
        },
        mockk { every { runBlocking { token() } }.returns("token") }
    )
}

private fun tomRespons() =
    """{
        "ident": {
            "identifikator": "fnr",
            "aktoerType": "NATURLIG_IDENT"
        }
    }"""

private fun responsMedInntekt() =
    """
        {"arbeidsInntektMaaned": [
                    {
                        "aarMaaned": "2018-12",
                        "arbeidsInntektInformasjon": {
                            "inntektListe": [
                                {
                                    "inntektType": "LOENNSINNTEKT",
                                    "beloep": 25000,
                                    "fordel": "kontantytelse",
                                    "inntektskilde": "A-ordningen",
                                    "inntektsperiodetype": "Maaned",
                                    "inntektsstatus": "LoependeInnrapportert",
                                    "leveringstidspunkt": "2020-01",
                                    "utbetaltIMaaned": "2018-12",
                                    "opplysningspliktig": {
                                        "identifikator": "orgnummer1",
                                        "aktoerType": "ORGANISASJON"
                                    },
                                    "virksomhet": {
                                        "identifikator": "orgnummer1",
                                        "aktoerType": "ORGANISASJON"
                                    },
                                    "inntektsmottaker": {
                                        "identifikator": "aktørId",
                                        "aktoerType": "AKTOER_ID"
                                    },
                                    "inngaarIGrunnlagForTrekk": true,
                                    "utloeserArbeidsgiveravgift": true,
                                    "informasjonsstatus": "InngaarAlltid",
                                    "beskrivelse": "fastloenn"
                                }
                            ]
                        }
                    }
                ]}
"""

private class Mock {
    fun get() = "{}"
}