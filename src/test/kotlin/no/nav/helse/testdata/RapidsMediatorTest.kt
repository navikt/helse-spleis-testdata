package no.nav.helse.testdata

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RapidsMediatorTest {
    private lateinit var testRapid: TestRapid
    private lateinit var rapidsMediator: RapidsMediator

    @BeforeEach
    fun beforeEach() {
        testRapid = TestRapid()
        rapidsMediator = RapidsMediator(object : RapidProducer {
            override fun publish(message: String) {
                testRapid.publish(message)
            }

            override fun publish(key: String, message: String) {
                testRapid.publish(key, message)
            }
        })
        testRapid.reset()
    }

    @Test
    fun `sender videre melding`() {
        rapidsMediator.publiser("partition_key", """{"key":"value"}""")
        assertEquals(1, testRapid.inspektør.size)
        assertEquals("partition_key", testRapid.inspektør.key(0))
        assertEquals("value", testRapid.inspektør.field(0, "key").asText())
    }

    @Test
    fun `Lager slettemelding for person`() {
        rapidsMediator.slett("12345678910")
        assertEquals(1, testRapid.inspektør.size)
        assertEquals("12345678910", testRapid.inspektør.key(0))
        assertEquals("slett_person", testRapid.inspektør.field(0, "@event_name").asText())
        assertEquals("12345678910", testRapid.inspektør.field(0, "fødselsnummer").asText())
    }
}