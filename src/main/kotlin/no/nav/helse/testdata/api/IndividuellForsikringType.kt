package no.nav.helse.testdata.api

/**
 * Speiler IndividuellForsikringType i sp-forsikring. [kode] er verdien Infotrygd lagrer i IF10_TYPE.
 */
enum class IndividuellForsikringType(val kode: Int) {
    SELVSTENDIG_80_PROSENT_FRA_DAG_1(1),
    SELVSTENDIG_100_PROSENT_FRA_DAG_17(2),
    SELVSTENDIG_100_PROSENT_FRA_DAG_1(3),
    SELVSTENDIG_JORDBRUKER_100_PROSENT_FRA_DAG_1(4),
    FRILANSER_100_PROSENT_FRA_DAG_1(5),
    ;

    companion object {
        fun fraKode(kode: Char): IndividuellForsikringType? = entries.firstOrNull { it.kode == kode.digitToIntOrNull() }
    }
}
