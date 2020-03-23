package no.nav.helse.testdata

import java.io.IOException
import java.lang.RuntimeException

fun assertValidJson(json: String) {
    try {
        objectMapper.readTree(json)
    } catch (e: IOException) {
        log.error("meldingen inneholder ugyldig JSON")
        throw RuntimeException("meldingen inneholder ugyldig JSON")
    }

}