package no.nav.helse.testdata

import org.junit.jupiter.api.Assertions.assertNotNull
import java.io.IOException
import java.lang.RuntimeException

fun assertValidJson(json: String?) {
    try {
        assertNotNull(json)
        objectMapper.readTree(json)
    } catch (e: IOException) {
        log.error("meldingen inneholder ugyldig JSON")
        throw RuntimeException("meldingen inneholder ugyldig JSON")
    }

}