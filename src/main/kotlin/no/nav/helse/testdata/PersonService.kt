package no.nav.helse.testdata

import kotliquery.Session
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
        val slettedeRader = using(sessionOf(spleisDataSource), {
            it.run(queryOf("delete from person where fnr = ?", fnr).asUpdate)
        })
        log.info("Slettet $slettedeRader testpersoner med fnr=$fnr fra Spleis")
    }

    private fun slettPersonFraSpesialist(fnr: String) {
        val fødselsnummer = fnr.toLong()
        val slettedeRader = using(sessionOf(spesialistDataSource)) { session ->
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

            @Language("PostgreSQL")
            val deletePersonConstraints = """
                DELETE FROM reserver_person WHERE person_ref=:personId;
                DELETE FROM digital_kontaktinformasjon WHERE person_ref=:personId;
                DELETE FROM gosysoppgaver WHERE person_ref=:personId;
                DELETE FROM egen_ansatt WHERE person_ref=:personId;
                DELETE FROM utbetaling WHERE utbetaling_id_ref in (select id from utbetaling_id where person_ref=:personId);
                DELETE FROM utbetaling_id WHERE person_ref=:personId;
            """
            session.run(queryOf(deletePersonConstraints, mapOf("personId" to personId)).asUpdate)

            slettFraUtbetalingstabeller(session, personId)

            session.run(queryOf("DELETE FROM person WHERE fodselsnummer = ?", fødselsnummer).asUpdate)
        }
        log.info("Slettet $slettedeRader testpersoner med fnr=$fnr fra Spesialist")
    }

    private fun slettFraUtbetalingstabeller(session: Session, personId: Int?) {
        @Language("PostgreSQL")
        val deleteUtbetalingslinje = """
            DELETE FROM utbetalingslinje where oppdrag_id in (SELECT person_fagsystem_id_ref FROM utbetaling_id WHERE person_ref=:personId);
            DELETE FROM utbetalingslinje where oppdrag_id in (SELECT arbeidsgiver_fagsystem_id_ref FROM utbetaling_id WHERE person_ref=:personId);
        """
        session.run(queryOf(deleteUtbetalingslinje, mapOf("personId" to personId)).asUpdate)

        @Language("PostgreSQL")
        val finnOppdragId = """
            SELECT unnest FROM utbetaling_id, unnest(array[person_fagsystem_id_ref, arbeidsgiver_fagsystem_id_ref]) WHERE person_ref=:personId
        """
        val oppdragIds = session.run(queryOf(finnOppdragId, mapOf("personId" to personId)).map { it.int(1) }.asList)

        @Language("PostgreSQL")
        val deleteUtbetaling = """
            DELETE FROM utbetaling WHERE utbetaling_id_ref in (SELECT id FROM utbetaling_id WHERE person_ref=:personId);
            DELETE FROM utbetaling_id WHERE person_ref=:personId;
        """
        session.run(queryOf(deleteUtbetaling, mapOf("personId" to personId)).asUpdate)

        @Language("PostgreSQL")
        val deleteOppdrag = """
            DELETE FROM oppdrag where id in (:oppdragIds);            
        """
        if (oppdragIds.isNotEmpty())
            session.run(
                queryOf(
                    deleteOppdrag,
                    mapOf("oppdragIds" to oppdragIds)
                ).asUpdate
            )
    }

    private fun slettPersonFraSpenn(fnr: String) {
        using(sessionOf(spennDataSource)) {
//            it.run(queryOf("").asUpdate)
        }
    }
}
