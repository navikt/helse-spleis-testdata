package no.nav.helse.testdata

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class PersonService(
    val spleisDataSource: DataSource,
    val spesialistDataSource: DataSource,
    val spennDataSource: DataSource
) {

    fun slett(fnr: String) {
        slettPersonFraSpleis(fnr)
        slettPersonFraSpesialist(fnr)
        slettPersonFraSpenn(fnr)
    }

    private fun slettPersonFraSpleis(fnr: String) {
        using(sessionOf(spleisDataSource), {
            it.run(queryOf("delete from person where fnr = ?", fnr).asUpdate)
        })
    }

    private fun slettPersonFraSpesialist(fnr: String) {
        val fødselsnummer = fnr.toLong()
        using(sessionOf(spesialistDataSource)) { session ->
            val personId = session.run(
                queryOf("SELECT id FROM person WHERE fodselsnummer = ?;", fødselsnummer)
                    .map { it.int(1) }.asSingle
            )

            val vedtakIder = session.run(
                queryOf("SELECT id, speil_snapshot_ref FROM vedtak WHERE person_ref = ?;", personId)
                    .map { Pair(it.int(1), it.int(2)) }.asList
            )

            if (vedtakIder.isNotEmpty()) {
                session.run(
                    queryOf(
                        """
                        DELETE FROM tildeling WHERE oppgave_id_ref IN (
                            SELECT id FROM oppgave WHERE vedtak_ref IN (${vedtakIder.joinToString { "?" }})
                        )""",
                        *vedtakIder.map { it.first }.toTypedArray()
                    ).asUpdate
                )
                session.run(
                    queryOf(
                        "DELETE FROM oppgave WHERE vedtak_ref in (${vedtakIder.joinToString { "?" }})",
                        *vedtakIder.map { it.first }.toTypedArray()
                    ).asUpdate
                )
                session.run(
                    queryOf(
                        "DELETE FROM automatisering WHERE vedtaksperiode_ref in (${vedtakIder.joinToString { "?" }})",
                        *vedtakIder.map { it.first }.toTypedArray()
                    ).asUpdate
                )
                @Language("PostgreSQL")
                val automatiseringProblemQuery =
                    """DELETE FROM automatisering_problem WHERE vedtaksperiode_ref in (${vedtakIder.joinToString { "?" }})"""
                session.run(
                    queryOf(automatiseringProblemQuery, *vedtakIder.map { it.first }.toTypedArray()).asUpdate
                )
            }

            val overstyringer = session.run(
                queryOf(
                    "SELECT * FROM overstyring WHERE person_ref = ?;",
                    personId
                ).map { it.long("id") }.asList
            )

            overstyringer.forEach { overstyringId ->
                session.run(
                    queryOf(
                        "DELETE FROM overstyrtdag WHERE overstyring_ref = ?;",
                        overstyringId
                    ).asUpdate
                )
                session.run(
                    queryOf(
                        "DELETE FROM overstyring WHERE id = ?;",
                        overstyringId
                    ).asUpdate
                )
            }

            session.run(queryOf("DELETE FROM vedtak WHERE person_ref = ?", personId).asUpdate)

            session.run(queryOf("DELETE FROM hendelse WHERE fodselsnummer = ?", fødselsnummer).asUpdate)

            if (vedtakIder.isNotEmpty()) {
                session.run(
                    queryOf(
                        "DELETE FROM speil_snapshot WHERE id in (${vedtakIder.joinToString { "?" }})",
                        *vedtakIder.map { it.second }.toTypedArray()
                    ).asUpdate
                )
            }

            session.run(queryOf("DELETE FROM reserver_person WHERE person_ref = ?", personId).asUpdate)

            session.run(queryOf("DELETE FROM person WHERE fodselsnummer = ?", fødselsnummer).asUpdate)
        }

    }

    private fun slettPersonFraSpenn(fnr: String) {
        using(sessionOf(spennDataSource)) {
//            it.run(queryOf("").asUpdate)
        }
    }
}
