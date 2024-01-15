package no.nav.helse.testdata

internal fun setUpEnvironment() =
    Environment(
        inntektRestUrl = System.getenv("INNTEKTSKOMPONENTEN_URL"),
        inntektScope = System.getenv("INNTEKTSKOMPONENTEN_SCOPE"),
        aaregUrl = System.getenv("AAREG_URL"),
        aaregScope = System.getenv("AAREG_SCOPE"),
        eregUrl = System.getenv("EREG_BASE_URL"),
        pdlUrl = System.getenv("PDL_BASE_URL"),
        pdlScope = System.getenv("PDL_SCOPE"),
        aktørRestUrl = "https://app-q1.adeo.no/aktoerregister/api/v1",
        dollyRestUrl = "https://dolly-backend.dev.intern.nav.no/api/v1",
    )

data class Environment(
    val inntektRestUrl: String,
    val inntektScope: String,
    val aaregUrl: String,
    val aaregScope: String,
    val eregUrl: String,
    val pdlUrl: String,
    val pdlScope: String,
    val aktørRestUrl: String,
    val dollyRestUrl: String,
)
