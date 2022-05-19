package no.nav.helse.testdata

import no.nav.helse.rapids_rivers.RapidsConnection
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.*

internal class RapidsMediator(private val connection: RapidsConnection) {
    internal fun publiser(nøkkel: String, melding: String) {
        connection.publish(nøkkel, melding)
    }

    internal fun slett(fødselsnummer: String) {
        connection.publish(fødselsnummer, slettPerson(fødselsnummer))
    }

    @Language("JSON")
    private fun slettPerson(fødselsnummer: String): String {
        return """
            {
              "@event_name": "slett_person",
              "@id": "${UUID.randomUUID()}",
              "@opprettet": "${LocalDateTime.now()}",
              "fødselsnummer": "$fødselsnummer"
            }
        """
    }
}