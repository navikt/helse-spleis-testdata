package no.nav.helse.testdata

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.testdata.Result.Ok
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.YearMonth

class InntektRestClientTest {

    @Test
    fun `person uten inntektshistorikk`() {
        val inntektRestClient = inntektRestClient(tomRespons())
        val inntektsliste = runBlocking {
            inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")
        }

        when (inntektsliste) {
            is Ok -> {
                assertNotNull(inntektsliste.value)
                assertEquals(0, inntektsliste.value.size)
            }
            else -> fail()
        }
    }

    @Test
    fun `person med inntektshistorikk`() {
        val inntektRestClient = inntektRestClient(responsMedInntekt())
        val inntektsliste = runBlocking {
            inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")
        }
        when (inntektsliste) {
            is Ok -> {
                assertNotNull(inntektsliste.value)
                assertEquals(1, inntektsliste.value.size)
            }
            else -> fail()
        }
    }



    private fun inntektRestClient(response: String) = InntektRestClient(
        "http://localhost.no",
        "clientId",
        MockAzureTokenProvider(),
        HttpClient(MockEngine) {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                }
            }
            engine {
                addHandler { request ->
                    if (request.url.fullPath.startsWith("/api/v1/hentinntektliste")) {
                        respond(response)
                    } else {
                        error("Endepunktet finnes ikke ${request.url.fullPath}")
                    }
                }
            }
        },
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
