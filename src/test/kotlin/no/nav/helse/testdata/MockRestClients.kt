package no.nav.helse.testdata

import io.ktor.client.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson3.*

internal val inntektRestClient =
    InntektRestClient(
        "http://localhost.no",
        "clientId",
        MockAzureTokenProvider(),
        HttpClient(MockEngine) {
            install(ContentNegotiation) {
                jackson()
            }
            engine {
                addHandler { request ->
                    if (request.url.fullPath.startsWith("/api/v1/hentinntektliste")) {
                        respond(
                            """{
                                "ident": {
                                "identifikator": "fnr",
                                "aktoerType": "NATURLIG_IDENT"
                            }
                        }""",
                        )
                    } else {
                        error("Endepunktet finnes ikke ${request.url.fullPath}")
                    }
                }
            }
        },
    )
