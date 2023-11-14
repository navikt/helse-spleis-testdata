package no.nav.helse.testdata

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.fullPath
import io.ktor.serialization.jackson.jackson

internal val inntektRestClient = InntektRestClient(
    "http://localhost.no",
    "clientId",
    { "token" },
    HttpClient(MockEngine) {
        install(ContentNegotiation) {
            jackson {
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                registerModule(JavaTimeModule())
            }
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
    }
)
