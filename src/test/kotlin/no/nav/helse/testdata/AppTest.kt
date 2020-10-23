package no.nav.helse.testdata

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import io.ktor.http.HttpMethod
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    companion object {
        private lateinit var personService: PersonService
        private const val fnr1 = "123"
        private const val fnr2 = "456"
    }

    private lateinit var spleisDB: EmbeddedPostgres
    private lateinit var spesialistDB: EmbeddedPostgres
    private lateinit var spennDB: EmbeddedPostgres
    private lateinit var postgresConnection: Connection
    private var producerMock = mockk<KafkaProducer<String, String>>(relaxed = true)

    @BeforeEach
    fun `start postgres`() {
        spleisDB = EmbeddedPostgres.builder().start()
        spesialistDB = EmbeddedPostgres.builder().start()
        spennDB = EmbeddedPostgres.builder().start()

        postgresConnection = spleisDB.postgresDatabase.connection

        runMigration(spleisDB, "spleis")
        runMigration(spesialistDB, "spesialist")
        runMigration(spennDB, "spenn")
        personService =
            PersonService(spleisDB.postgresDatabase, spesialistDB.postgresDatabase, spennDB.postgresDatabase)
    }

    @AfterEach
    fun `stop postgres`() {
        spleisDB.close()
    }

    @Test
    fun `slett person`() {
        opprettPerson(fnr1)
        opprettPerson(fnr1)
        opprettPerson(fnr2)

        withTestApplication({
            routing {
                registerPersonApi(personService, aktørRestClient)
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
                        "inntekt": 0.0,
                        "harAndreInntektskilder": true,
                        "skalSendeInntektsmelding": true,
                        "førstefraværsdag": "2019-12-31",
                        "arbeidsgiverperiode": [{"fom": "2019-12-31", "tom": "2020-01-14"}, {"fom": "2019-12-31", "tom": "2020-01-14"}],
                        "ferieperioder": []
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

    @Test
    fun `slå opp aktørId`() {
        withTestApplication({
            installJacksonFeature()
            routing {
                registerPersonApi(personService, aktørRestClient)
            }
        }) {
            with(handleRequest(HttpMethod.Get, "/person/aktorid") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                addHeader("ident", "fnr")
            }) {
                assertTrue(response.status()!!.isSuccess())
            }
        }
    }

    private fun opprettPerson(fnr: String) {
        opprettSpleisPerson(fnr)
        opprettSpesialistPerson(fnr)
    }

    private fun opprettSpleisPerson(fnr: String) {
        using(sessionOf(spleisDB.postgresDatabase), {
            it.run(
                queryOf(
                    "insert into person (aktor_id, fnr, skjema_versjon, data) values ('aktørId', ?, 4, (to_json(?::json)))",
                    fnr,
                    "{}"
                ).asUpdate
            )
        })
    }

    private fun finnPerson(fnr: String) = using(sessionOf(spesialistDB.postgresDatabase)) { session ->
        session.run(
            queryOf("select id from person where fodselsnummer = ?", fnr.toLong()).map { it.int(1) }.asSingle
        )
    }

    private fun opprettSpesialistPerson(fnr: String) {
        using(sessionOf(spesialistDB.postgresDatabase, returnGeneratedKey = true)) {

            val personId = finnPerson(fnr) ?: it.run(
                queryOf(
                    "insert into person (fodselsnummer, aktor_id) values (?, ?)",
                    fnr.toLong(),
                    fnr.reversed().toLong()
                ).asUpdateAndReturnGeneratedKey
            )
            val speilSnapshotId = it.run(
                queryOf(
                    "insert into speil_snapshot (data) values ('some data')"
                ).asUpdateAndReturnGeneratedKey
            )
            val vedtakId = it.run(
                queryOf(
                    "insert into vedtak (person_ref, speil_snapshot_ref) values (?, ?)", personId, speilSnapshotId
                ).asUpdateAndReturnGeneratedKey
            )
            val overstyringId = it.run(
                queryOf(
                    "insert into overstyring(person_ref) values (?)", personId
                ).asUpdateAndReturnGeneratedKey
            )
            it.run(
                queryOf(
                    "insert into overstyrtdag(overstyring_ref) values (?)", overstyringId
                ).asUpdate
            )
            it.run(
                queryOf(
                    "insert into oppgave (vedtak_ref) values(?)", vedtakId
                ).asUpdate
            )
            it.run(
                queryOf(
                    "INSERT INTO automatisering (vedtaksperiode_ref) VALUES (?)", vedtakId
                ).asUpdate
            )
        }
    }

    private fun antallRader(fnr: String): Int {
        return using(sessionOf(spleisDB.postgresDatabase), { session ->
            session.run(queryOf("select * from person where fnr = ?", fnr).map {
                it.string("fnr")
            }.asList).size
        })
    }

}
