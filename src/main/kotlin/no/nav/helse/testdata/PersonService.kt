package no.nav.helse.testdata

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class PersonService(val spennDataSource: DataSource, private val rapidsMediator: RapidsMediator) {

    fun slett(fnr: String) {
        rapidsMediator.slett(fnr)
        slettPersonFraSpenn(fnr)
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
