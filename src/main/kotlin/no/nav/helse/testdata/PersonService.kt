package no.nav.helse.testdata

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource

class PersonService(val dataSource: DataSource) {

    fun slett(fnr: String) {
        using(sessionOf(dataSource), {
            it.run(queryOf("delete from person where fnr = ?", fnr).asUpdate)
        })
    }

}