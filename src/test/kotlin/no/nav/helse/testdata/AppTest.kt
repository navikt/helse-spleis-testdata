package no.nav.helse.testdata

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.HttpMethod
import io.ktor.http.fullPath
import io.ktor.http.isSuccess
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.sql.Connection

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
            with(handleRequest(HttpMethod.Delete, "/person") {
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
                registerVedtaksperiodeApi(
                    producer = producerMock,
                    aktørRestClient = mockk { every { runBlocking { hentAktørId(any()) } }.returns(Result.Ok("aktørId")) })
            }
        }) {
            with(handleRequest(HttpMethod.Post, "/vedtaksperiode") {
                addHeader("Content-Type", "application/json")
                setBody(
                    """
                    {
                        "fnr": "fnr",
                        "orgnummer": "orgnummer",
                        "sykdomFom": "2020-01-10",
                        "sykdomTom": "2020-01-30",
                        "inntekt": 0.0
                    }
                    """
                )
            }) {
                assertTrue(response.status()!!.isSuccess())
            }
        }
    }

    @Test
    fun `slå opp inntekt`() {
        withTestApplication({
            installJacksonFeature()
            routing {
                registerInntektsApi(inntektRestClient)
            }
        }) {
            with(handleRequest(HttpMethod.Get, "/person/inntekt") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                addHeader("ident", "fnr")
            }) {
                assertTrue(response.status()!!.isSuccess())
            }
        }
    }

    private val inntektRestClient = InntektRestClient(
        "http://localhost.no", HttpClient(MockEngine) {
            install(JsonFeature) {
                this.serializer = JacksonSerializer()
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
        },
        mockk { every { runBlocking { token() } }.returns("token") }
    )

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