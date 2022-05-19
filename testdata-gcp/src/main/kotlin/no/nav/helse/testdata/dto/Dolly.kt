package no.nav.helse.testdata.dto

import java.time.LocalDate


data class DollyBruker(
    val brukerId: String,
    val brukernavn: String,
    val brukertype: DollyBrukertype,
    val epost: String,
    val navIdent: String,
) {
    enum class DollyBrukertype {
        AZURE, BANKID, BASIC
    }
}

data class DollyIdentBestilling(
    val ident: String,
    val beskrivelse: String,
    val bestillingId: List<Int>,
    val master: DollyIdentBestillingMaster,
    val ibruk: Boolean,
) {
    enum class DollyIdentBestillingMaster {
        PDL, PDLF, TPSF
    }
}

data class DollyTestgruppe(
    val id: Int,
    val navn: String,
    val hensikt: String,
    val opprettetAv: DollyBruker,
    val sistEndretAv: DollyBruker,
    val tags: List<DollyTestGruppeTags>,
    val datoEndret: LocalDate,
    val antallIdenter: Int,
    val antallIBruk: Int,
    val erEierAvGruppe: Boolean,
    val favorittIGruppen: Boolean,
    val erLaast: Boolean,
    val laastBeskrivelse: String,
    val identer: List<DollyIdentBestilling>
) {
    enum class DollyTestGruppeTags {
        SALESFORCE, DOLLY
    }
}
