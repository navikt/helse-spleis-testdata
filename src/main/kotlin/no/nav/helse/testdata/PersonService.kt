package no.nav.helse.testdata

import kotliquery.*
import org.intellij.lang.annotations.Language
import java.util.*
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
        val fødselsnummer = fnr.toLong()
        sessionOf(spleisDataSource).use {
            it.transaction { transactionalSession ->
                val slettedeRader =
                    transactionalSession.run(queryOf("delete from person where fnr = ?", fødselsnummer).asUpdate)
                log.info("Slettet $slettedeRader testpersoner med fnr=$fnr fra Spleis")
                val slettedeMeldinger =
                    transactionalSession.run(queryOf("delete from melding where fnr = ?", fødselsnummer).asUpdate)
                log.info("Slettet $slettedeMeldinger meldinger for fnr=$fnr fra Spleis")
            }
        }
    }

    private data class Vedtak(
        val id: Int,
        val speilSnapshotRef: Int,
        val vedtaksperiodeId: UUID,
    )

    private fun slettPersonFraSpesialist(fnr: String) {
        val fødselsnummer = fnr.toLong()
        val slettedeRader = sessionOf(spesialistDataSource).transaction { transactionalSession ->
            val personId = transactionalSession.run(
                queryOf("SELECT id FROM person WHERE fodselsnummer = ?;", fødselsnummer)
                    .map { it.int(1) }.asSingle
            )

            val vedtak = transactionalSession.run(
                queryOf("SELECT id, speil_snapshot_ref, vedtaksperiode_id FROM vedtak WHERE person_ref = ?;", personId)
                    .map {
                        Vedtak(
                            it.int("id"),
                            it.int("speil_snapshot_ref"),
                            UUID.fromString(it.string("vedtaksperiode_id"))
                        )
                    }.asList
            )

            if (vedtak.isNotEmpty()) {
                transactionalSession.run(
                    queryOf(
                        """
                        DELETE FROM tildeling WHERE oppgave_id_ref IN (
                            SELECT id FROM oppgave WHERE vedtak_ref IN (${vedtak.joinToString { "?" }})
                        )""",
                        *vedtak.map { it.id }.toTypedArray()
                    ).asUpdate
                )
                transactionalSession.run(
                    queryOf(
                        "DELETE FROM oppgave WHERE vedtak_ref in (${vedtak.joinToString { "?" }})",
                        *vedtak.map { it.id }.toTypedArray()
                    ).asUpdate
                )
                transactionalSession.run(
                    queryOf(
                        "DELETE FROM automatisering WHERE vedtaksperiode_ref in (${vedtak.joinToString { "?" }})",
                        *vedtak.map { it.id }.toTypedArray()
                    ).asUpdate
                )
                @Language("PostgreSQL")
                val automatiseringProblemQuery =
                    """DELETE FROM automatisering_problem WHERE vedtaksperiode_ref in (${vedtak.joinToString { "?" }})"""
                transactionalSession.run(
                    queryOf(automatiseringProblemQuery, *vedtak.map { it.id }.toTypedArray()).asUpdate
                )

                transactionalSession.run(
                    queryOf(
                        "DELETE FROM notat WHERE vedtaksperiode_id in (${vedtak.joinToString { "?" }})",
                        *vedtak.map { it.vedtaksperiodeId }.toTypedArray()
                    ).asUpdate
                )
            }

            val overstyringer = transactionalSession.run(
                queryOf(
                    "SELECT * FROM overstyring WHERE person_ref = ?;",
                    personId
                ).map { it.long("id") }.asList
            )

            overstyringer.forEach { overstyringId ->
                transactionalSession.run(
                    queryOf(
                        "DELETE FROM overstyrtdag WHERE overstyring_ref = ?;",
                        overstyringId
                    ).asUpdate
                )
                transactionalSession.run(
                    queryOf(
                        "DELETE FROM overstyring WHERE id = ?;",
                        overstyringId
                    ).asUpdate
                )
            }

            transactionalSession.run(queryOf("DELETE FROM vedtak WHERE person_ref = ?", personId).asUpdate)

            transactionalSession.run(queryOf("DELETE FROM hendelse WHERE fodselsnummer = ?", fødselsnummer).asUpdate)

            if (vedtak.isNotEmpty()) {
                transactionalSession.run(
                    queryOf("DELETE FROM speil_snapshot WHERE person_ref = ?", personId).asUpdate
                )
            }

            @Language("PostgreSQL")
            val deletePersonConstraints = """
                DELETE FROM reserver_person WHERE person_ref=:personId;
                DELETE FROM digital_kontaktinformasjon WHERE person_ref=:personId;
                DELETE FROM gosysoppgaver WHERE person_ref=:personId;
                DELETE FROM egen_ansatt WHERE person_ref=:personId;
                DELETE FROM arbeidsforhold WHERE person_ref=:personId;
                DELETE FROM abonnement_for_opptegnelse WHERE person_id=:personId;
                DELETE FROM opptegnelse WHERE person_id=:personId;
            """
            transactionalSession.run(queryOf(deletePersonConstraints, mapOf("personId" to personId)).asUpdate)

            slettFraUtbetalingstabeller(transactionalSession, personId)

            transactionalSession.run(queryOf("DELETE FROM person WHERE fodselsnummer = ?", fødselsnummer).asUpdate)
        }
        log.info("Slettet $slettedeRader testpersoner med fnr=$fnr fra Spesialist")
    }

    private fun slettFraUtbetalingstabeller(session: TransactionalSession, personId: Int?) {
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

        if (oppdragIds.isNotEmpty()) {
            @Language("PostgreSQL")
            val deleteOppdrag = """
                DELETE FROM oppdrag where id in (${oppdragIds.joinToString { "?" }});
            """
            session.run(queryOf(deleteOppdrag, *oppdragIds.toTypedArray()).asUpdate)
        }
    }

    private fun slettPersonFraSpenn(fnr: String) {
        val slettedeRader = sessionOf(spennDataSource).use {
            @Language("PostgreSQL")
            val statement = "DELETE FROM oppdrag WHERE fnr = ?"
            it.run(queryOf(statement, fnr).asUpdate)
        }
        log.info("Slettet $slettedeRader testpersoner med fnr=$fnr fra Spenn")
    }
}
