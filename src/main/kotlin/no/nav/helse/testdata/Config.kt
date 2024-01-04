package no.nav.helse.testdata

import java.net.URI

internal fun setUpEnvironment() =
    Environment(
        aadTokenEndpoint = URI(System.getenv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
        aadClientId = System.getenv("AZURE_APP_CLIENT_ID"),
        aadClientSecret = System.getenv("AZURE_APP_CLIENT_SECRET"),
        inntektRestUrl = System.getenv("INNTEKTSKOMPONENTEN_URL"),
        inntektScope = System.getenv("INNTEKTSKOMPONENTEN_SCOPE"),
        aaregUrl = System.getenv("AAREG_URL"),
        aaregScope = System.getenv("AAREG_SCOPE"),
        aktørRestUrl = "https://app-q1.adeo.no/aktoerregister/api/v1",
        dollyRestUrl = "https://dolly-backend.dev.intern.nav.no/api/v1",
    )

data class Environment(
    val aadTokenEndpoint: URI,
    val aadClientId: String,
    val aadClientSecret: Any,
    val inntektRestUrl: String,
    val inntektScope: String,
    val aaregUrl: String,
    val aaregScope: String,
    val aktørRestUrl: String,
    val dollyRestUrl: String,
)
