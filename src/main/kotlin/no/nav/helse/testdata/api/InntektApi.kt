package no.nav.helse.testdata.api

import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import no.nav.helse.testdata.InntektRestClient
import no.nav.helse.testdata.Result
import no.nav.helse.testdata.log
import java.time.YearMonth
import java.util.*

private const val InntekterForSykepengegrunnlag = "8-28"

@Suppress("unused")
private const val InntekterForSammenligningsgrunnlag = "8-30"

internal fun Routing.registerInntektApi(inntektRestClient: InntektRestClient) = get("/person/inntekt") {
    val fnr = requireNotNull(call.request.header("ident")) { "Mangler header: [ident: fnr]" }
    val end = YearMonth.now().minusMonths(1)
    val start = end.minusMonths(11)
    val inntekterResult = inntektRestClient.hentInntektsliste(
        fnr = fnr,
        fom = start,
        tom = end,
        filter = InntekterForSykepengegrunnlag,
        callId = UUID.randomUUID().toString()
    )
    when (inntekterResult) {
        is Result.Ok -> {
            val inntekterPerAg = inntekterResult.value
                .flatMap { it.inntektsliste }
                .groupBy { it.orgnummer }
                .mapValues { it.value.sumOf { it.beløp } / 12.0 }
                .mapValues { (_, månedsinntekt) ->
                    (månedsinntekt * 100).toInt() / 100.0 // bevarer to desimaler, fjerner resten
                }
                .map { (orgnummer, beregnetInntekt) ->
                    mapOf(
                        "organisasjonsnummer" to orgnummer,
                        "beregnetMånedsinntekt" to beregnetInntekt
                    )
                }
            call.respond(mapOf("arbeidsgivere" to inntekterPerAg))
        }

        is Result.Error -> {
            log.info("Henting av inntekt feilet: ${inntekterResult.error}")
            call.respond(inntekterResult.error.statusCode, inntekterResult.error.response)
        }
    }
}
