package no.nav.helse.testdata

import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.testdata.api.registerInntektApi
import no.nav.helse.testdata.api.registerPersonApi
import no.nav.helse.testdata.api.registerVedtaksperiodeApi
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    companion object {
        private lateinit var personService: PersonService
        private const val fnr1 = "123"
        private const val fnr2 = "456"
    }

    private lateinit var psqlContainer: PostgreSQLContainer<Nothing>
    private lateinit var spleisDataSource: DataSource
    private lateinit var spesialistDataSource: DataSource
    private lateinit var spennDataSource: DataSource
    private lateinit var rapidsConnection: RapidsConnection

    @BeforeAll
    fun beforeAll() {
        psqlContainer = PostgreSQLContainer<Nothing>("postgres:12").withInitScript("create_databases.sql")
        psqlContainer.start()
    }

    @BeforeEach
    fun `start postgres`() {

        spleisDataSource = runMigration(psqlContainer, "spleis")
        spesialistDataSource = runMigration(psqlContainer, "spesialist")
        spennDataSource = runMigration(psqlContainer, "spenn")

        rapidsConnection = TestRapid()

        runMigration(psqlContainer, "spleis")
        runMigration(psqlContainer, "spesialist")
        runMigration(psqlContainer, "spenn")
        personService =
            PersonService(spleisDataSource, spesialistDataSource, spennDataSource)
    }

    @AfterAll
    fun afterAll() {
        psqlContainer.close()
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
                    mediator = RapidsMediator(rapidsConnection),
                    aktørRestClient = mockk { coEvery { hentAktørId(any()) }.returns(Result.Ok("aktørId")) })
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
                registerInntektApi(inntektRestClient)
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
        using(sessionOf(spleisDataSource)) {
            it.run(
                queryOf(
                    "insert into person (aktor_id, fnr, skjema_versjon, data) values ('aktørId', ?, 4, (to_json(?::json)))",
                    fnr.toLong(),
                    "{}"
                ).asUpdate
            )
            it.run(
                queryOf(
                    """
                        insert into melding (fnr, melding_id, melding_type, data, lest_dato)
                        values (?, '${UUID.randomUUID()}', 'melding_type', '{}', now())""",
                    fnr.toLong()
                ).asUpdate
            )
        }
    }

    private fun finnPerson(fnr: String) = using(sessionOf(spesialistDataSource)) { session ->
        session.run(
            queryOf("select id from person where fodselsnummer = ?", fnr.toLong()).map { it.int(1) }.asSingle
        )
    }

    private fun opprettSpesialistPerson(fnr: String) {
        using(sessionOf(spesialistDataSource, returnGeneratedKey = true)) {

            val personId = finnPerson(fnr) ?: it.run(
                queryOf(
                    "insert into person (fodselsnummer, aktor_id) values (?, ?)",
                    fnr.toLong(),
                    fnr.reversed().toLong()
                ).asUpdateAndReturnGeneratedKey
            )
            val speilSnapshotId = it.run(
                queryOf(
                    "insert into speil_snapshot (data, person_ref) values ('some data', ?) ON CONFLICT (person_ref) DO UPDATE SET data = 'some new data'",
                    personId
                ).asUpdateAndReturnGeneratedKey
            )
            val vedtaksperiodeId = UUID.randomUUID()
            val vedtakId = it.run(
                queryOf(
                    "insert into vedtak (person_ref, speil_snapshot_ref, vedtaksperiode_id) values (?, ?, ?)",
                    personId,
                    speilSnapshotId,
                    vedtaksperiodeId
                ).asUpdateAndReturnGeneratedKey
            )
            val overstyringId = it.run(
                queryOf(
                    "insert into overstyring(person_ref) values (?)", personId
                ).asUpdateAndReturnGeneratedKey
            )

            @Language("PostgreSQL")
            val overstyringInntektQuery = """INSERT INTO overstyring_inntekt(person_ref) VALUES (?)"""
            it.run(
                queryOf(overstyringInntektQuery, personId).asUpdate
            )

            @Language("PostgreSQL")
            val overstyringArbeidsforholdQuery = """INSERT INTO overstyring_arbeidsforhold(person_ref) VALUES (?)"""
            it.run(
                queryOf(overstyringArbeidsforholdQuery, personId).asUpdate
            )

            @Language("PostgreSQL")
            val digitalKontaktinformasjonQuery = """INSERT INTO digital_kontaktinformasjon(person_ref) VALUES (?)"""
            it.run(
                queryOf(digitalKontaktinformasjonQuery, personId).asUpdate
            )

            @Language("PostgreSQL")
            val gosysoppgaverQuery = """INSERT INTO gosysoppgaver(person_ref) VALUES (?)"""
            it.run(
                queryOf(gosysoppgaverQuery, personId).asUpdate
            )

            @Language("PostgreSQL")
            val egenAnsattQuery = """INSERT INTO egen_ansatt(person_ref) VALUES (?)"""
            it.run(
                queryOf(egenAnsattQuery, personId).asUpdate
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

            val saksbehandleroid = UUID.randomUUID()
            it.run(
                queryOf(
                    "INSERT INTO saksbehandler (oid, navn, epost) VALUES (?, ?, ?)",
                    saksbehandleroid,
                    "En Saksbehandler",
                    "saksbehandler@nav.no"
                ).asUpdate
            )

            it.run(
                queryOf(
                    "INSERT INTO notat (tekst, saksbehandler_oid, vedtaksperiode_id) VALUES('some text', ?, ?)",
                    saksbehandleroid,
                    vedtaksperiodeId
                ).asUpdate
            )

            @Language("PostgreSQL")
            val hendelseQuery = """INSERT INTO hendelse(fodselsnummer) VALUES (?)"""
            val hendelseId = it.run(
                queryOf(hendelseQuery, fnr.toLong()).asUpdateAndReturnGeneratedKey
            )

            @Language("PostgreSQL")
            val automatiseringProblemQuery =
                """INSERT INTO automatisering_problem(vedtaksperiode_ref, hendelse_ref) VALUES (:vedtaksperiode_ref, :hendelse_ref)"""
            it.run(
                queryOf(
                    automatiseringProblemQuery,
                    mapOf("vedtaksperiode_ref" to vedtakId, "hendelse_ref" to hendelseId)
                ).asUpdate
            )

            @Language("PostgreSQL")
            val arbeidsforholdquery =
                """INSERT INTO arbeidsforhold(person_ref) VALUES (:person_ref)"""
            it.run(
                queryOf(
                    arbeidsforholdquery,
                    mapOf("person_ref" to personId)
                ).asUpdate
            )
        }
    }

    private fun antallRader(fnr: String): Int {
        return using(sessionOf(spleisDataSource)) { session ->
            session.run(queryOf("select * from person where fnr = ?", fnr.toLong()).map {
                it.long("fnr")
            }.asList).size
        }
    }

}
