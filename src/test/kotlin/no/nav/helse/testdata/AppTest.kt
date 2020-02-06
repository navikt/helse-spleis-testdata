package no.nav.helse.testdata

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppTest {

    companion object {
        private lateinit var personService: PersonService
        private const val fnr1 = "123"
        private const val fnr2 = "456"
    }

    private lateinit var embeddedPostgres: EmbeddedPostgres
    private lateinit var postgresConnection: Connection
    private var producerMock = mockk<KafkaProducer<String, String>>(relaxed = true)

    @BeforeEach
    fun `start postgres`() {
        embeddedPostgres = EmbeddedPostgres.builder().start()

        postgresConnection = embeddedPostgres.postgresDatabase.connection

        runMigration(embeddedPostgres)
        personService = PersonService(embeddedPostgres.postgresDatabase)
    }

    @AfterEach
    fun `stop postgres`() {
        embeddedPostgres.close()
    }

    @Test
    fun `slett person`() {
        opprettPerson(fnr1)
        opprettPerson(fnr2)

        withTestApplication({
            routing {
                registerPersonApi(personService)
            }
        }) {
            with(handleRequest(HttpMethod.Delete, "/person"){
                addHeader("ident", fnr1)
            }) {

                assertTrue(response.status()!!.isSuccess())
                assertEquals(0, antallRader(fnr1))
                assertEquals(1, antallRader(fnr2))
            }
        }
    }

    @Test
    fun `opprett vedtak`() {
        withTestApplication({
            installJacksonFeature()
            routing {
                registerVedtaksperiodeApi(producerMock)
            }
        }) {
            with(handleRequest(HttpMethod.Post, "/vedtaksperiode") {
                addHeader("Content-Type", "application/json")
                setBody(
                    """
                    {
                        "aktørId": "123",
                        "fnr": "fnr",
                        "orgnummer": "orgnummer",
                        "sykdomFom": "2020-01-10",
                        "sykdomTom": "2020-01-30"
                    }
                    """
                )
            }) {
                assertTrue(response.status()!!.isSuccess())
            }
        }
    }

    private fun opprettPerson(fnr: String) {
        using(sessionOf(embeddedPostgres.postgresDatabase), {
            it.run(
                queryOf(
                    "insert into person (aktor_id, fnr, skjema_versjon, data) values ('aktørId', ?, 4, (to_json(?::json)))",
                    fnr,
                    "{}"
                ).asUpdate
            )
        })
    }

    private fun antallRader(fnr: String): Int {
        return using(sessionOf(embeddedPostgres.postgresDatabase), { session ->
            session.run(queryOf("select * from person where fnr = ?", fnr).map {
                it.string("fnr")
            }.asList).size
        })
    }

}