package no.nav.helse.testdata

internal fun setUpEnvironment() =
    Environment(
        inntektRestUrl = System.getenv("INNTEKTSKOMPONENTEN_URL"),
        inntektScope = System.getenv("INNTEKTSKOMPONENTEN_SCOPE"),
        aaregUrl = System.getenv("AAREG_URL"),
        aaregScope = System.getenv("AAREG_SCOPE"),
        eregUrl = System.getenv("EREG_BASE_URL")
    )

data class Environment(
    val inntektRestUrl: String,
    val inntektScope: String,
    val aaregUrl: String,
    val aaregScope: String,
    val eregUrl: String
)
