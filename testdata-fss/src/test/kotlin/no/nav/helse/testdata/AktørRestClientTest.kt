package no.nav.helse.testdata

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AktørRestClientTest {

    @Test
    fun `henter aktørId`() = runBlocking {
        val aktørKlient = aktørKlient {
            respond("""
                {
                    "fnr": {
                        "identer": [
                            {
                                "ident": "aktørId",
                                "identgruppe": "AKTOR_ID",
                                "gjeldende": true
                            }
                        ]
                    }
                }""", headers = headersOf("Content-Type" to listOf("application/json")))
        }

        assertEquals(Result.Ok<String, Exception>("aktørId"), aktørKlient.hentAktørId("fnr"))
    }

    private fun aktørKlient(config: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) = AktørRestClient("unused", HttpClient(MockEngine) {
        install(ContentNegotiation) { jackson() }
        engine {
            addHandler(config)
        }
    }, mockk { every { runBlocking { token() } }.returns("token") })
}