package no.nav.helse.testdata

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
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
        using(sessionOf(spesialistDataSource)) { session ->
            val personId = session.run(
                queryOf("SELECT id FROM person WHERE fodselsnummer = ?;", Integer.parseInt(fnr))
                    .map { it.int(0) }.asSingle
            )

            val vedtakIder = session.run(
                queryOf("SELECT id, speil_snapshot_ref FROM vedtak WHERE person_ref = ?;", personId)
                    .map { Pair(it.int(0), it.int(1)) }.asSingle
            )

            vedtakIder?.let { (vedtakId, speilSnapshotRef) ->
                session.run(queryOf("DELETE FROM speil_snapshot WHERE id = ?", vedtakId).asExecute)
                session.run(queryOf("DELETE FROM oppgave WHERE vedtak_ref = ?", speilSnapshotRef).asExecute)
            }

            if (personId != null) session.run(queryOf("DELETE FROM vedtak WHERE person_ref = ?", personId).asExecute)

            session.run(queryOf("DELETE FROM person WHERE fodselsnummer = ?", Integer.parseInt(fnr)).asExecute)
        }
    }

    private fun slettPersonFraSpenn(fnr: String) {
        using(sessionOf(spennDataSource)) {
//            it.run(queryOf("").asUpdate)
        }
    }
}