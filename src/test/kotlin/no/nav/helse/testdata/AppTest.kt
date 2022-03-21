package no.nav.helse.testdata

import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.testdata.api.registerInntektApi
import no.nav.helse.testdata.api.registerPersonApi
import no.nav.helse.testdata.api.registerVedtaksperiodeApi
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    companion object {
        private lateinit var personService: PersonService
        private const val fnr1 = "123"
    }

    private lateinit var psqlContainer: PostgreSQLContainer<Nothing>
    private lateinit var spennDataSource: DataSource
    private lateinit var testRapid: TestRapid

    @BeforeAll
    fun beforeAll() {
        psqlContainer = PostgreSQLContainer<Nothing>("postgres:12").withInitScript("create_databases.sql")
        psqlContainer.start()
    }

    @BeforeEach
    fun `start postgres`() {

        spennDataSource = runMigration(psqlContainer, "spenn")

        testRapid = TestRapid()

        personService = PersonService(
            spennDataSource = spennDataSource,
            rapidsMediator = RapidsMediator(testRapid)
        )
    }

    @AfterAll
    fun afterAll() {
        psqlContainer.close()
    }

    @Test
    fun `slett person`() {
        withTestApplication({
            routing {
                registerPersonApi(personService, aktørRestClient)
            }
        }) {
            with(handleRequest(HttpMethod.Delete, "/person") {
                addHeader("ident", fnr1)
            }) {
                assertTrue(response.status()!!.isSuccess())
                assertEquals(1, testRapid.inspektør.size)
                assertEquals("slett_person", testRapid.inspektør.field(0, "@event_name").asText())
            }
        }
    }

    @Test
    fun `opprett vedtak`() {

        withTestApplication({
            installJacksonFeature()
            routing {
                registerVedtaksperiodeApi(
                    mediator = RapidsMediator(testRapid),
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
}
