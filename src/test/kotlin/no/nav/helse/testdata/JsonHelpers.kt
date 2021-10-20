package no.nav.helse.testdata

import org.junit.jupiter.api.assertDoesNotThrow

fun assertValidJson(json: String?) {
    assertDoesNotThrow { objectMapper.readTree(json) }
}