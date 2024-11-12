package no.nav.helse.testdata

import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

interface RapidProducer {
    fun publish(message: String)
    fun publish(key: String, message: String)
}
internal class RapidsMediator(private val producer: RapidProducer) {
    private companion object {
        private val logg: Logger = LoggerFactory.getLogger(RapidsMediator::class.java)
    }
    internal fun publiser(nøkkel: String, melding: String) {
        logg.info("publiserer syntetisk testdatamelding key=$nøkkel record:\n$melding")
        producer.publish(nøkkel, melding)
    }

    internal fun slett(fødselsnummer: String) {
        producer.publish(fødselsnummer, slettPerson(fødselsnummer))
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