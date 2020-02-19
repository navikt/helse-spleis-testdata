package no.nav.helse.testdata

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

internal val inntektRestClient = InntektRestClient(
    "http://localhost.no", HttpClient(MockEngine) {
        install(JsonFeature) {
            this.serializer = JacksonSerializer()
        }
        engine {
            addHandler { request ->
                if (request.url.fullPath.startsWith("/api/v1/hentinntektliste")) {
                    respond("""{
                                "ident": {
                                "identifikator": "fnr",
                                "aktoerType": "NATURLIG_IDENT"
                            }
                        }""")
                } else {
                    error("Endepunktet finnes ikke ${request.url.fullPath}")
                }
            }
        }
    },
    mockk { every { runBlocking { token() } }.returns("token") }
)

internal val aktørRestClient = AktørRestClient(
    "http://localhost.no", HttpClient(MockEngine) {
        install(JsonFeature) {
            this.serializer = JacksonSerializer()
        }
        engine {
            addHandler { request ->
                if (request.url.fullPath.startsWith("/identer")) {
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
                }""", headers = headersOf("Content-Type" to listOf("application/json"))
                    )
                } else {
                    error("Endepunktet finnes ikke ${request.url.fullPath}")
                }
            }
        }
    },
    mockk { every { runBlocking { token() } }.returns("token") }
)