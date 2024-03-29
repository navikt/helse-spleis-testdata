package no.nav.helse.testdata

import org.junit.jupiter.api.assertDoesNotThrow

fun assertValidJson(json: String?) {
    assertDoesNotThrow {
        println("<$json>")
        objectMapper.readTree(json)
    }
}