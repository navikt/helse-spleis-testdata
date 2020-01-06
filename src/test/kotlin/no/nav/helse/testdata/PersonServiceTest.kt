package no.nav.helse.testdata

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection

class PersonServiceTest {

    companion object {
        private lateinit var personService: PersonService
        private lateinit var embeddedPostgres: EmbeddedPostgres
        private lateinit var postgresConnection: Connection
        private lateinit var hikariConfig: HikariConfig
    }

    @BeforeEach
    fun `start postgres`() {
        embeddedPostgres = EmbeddedPostgres.builder().start()
        runMigration()

        postgresConnection = embeddedPostgres.postgresDatabase.connection
        hikariConfig = createHikariConfig(embeddedPostgres.getJdbcUrl("postgres", "postgres"))

        personService = PersonService(embeddedPostgres.postgresDatabase)
    }

    @AfterEach
    fun `stop postgres`() {
        postgresConnection.close()
        embeddedPostgres.close()
    }

    @Test
    internal fun `slett person`() {
        val aktørId = "123456789"

        opprettPerson(aktørId)
        assertEquals(1, antallRader(aktørId))

        personService.slett(aktørId)

        assertEquals(0, antallRader(aktørId))
    }

    private fun runMigration() =
        Flyway.configure()
            .dataSource(HikariDataSource(hikariConfig))
            .load()
            .migrate()

    private fun createHikariConfig(jdbcUrl: String) =
        HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            maximumPoolSize = 3
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }

    private fun opprettPerson(aktørId: String) {
        using(sessionOf(embeddedPostgres.postgresDatabase), {
            it.run(queryOf("insert into person (aktor_id, data) values (?, ?)", aktørId, "{}").asUpdate)
        })
    }

    private fun antallRader(aktørId: String): Int {
        return using(sessionOf(embeddedPostgres.postgresDatabase), { session ->
            session.run(queryOf("select * from person where aktor_id = '?'", aktørId).map {
                it.string("aktor_id")
            }.asList).size
        })
    }

}