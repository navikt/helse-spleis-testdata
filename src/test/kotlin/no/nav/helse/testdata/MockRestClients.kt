package no.nav.helse.testdata

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*

internal val inntektRestClient = InntektRestClient(
    "http://localhost.no",
    "clientId",
    MockAzureTokenProvider(),
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
