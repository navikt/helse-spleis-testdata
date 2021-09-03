package no.nav.helse.testdata.api

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.testdata.InntektRestClient
import no.nav.helse.testdata.Result
import java.time.YearMonth
import java.util.*
import kotlin.math.round


internal fun Routing.registerInntektApi(inntektRestClient: InntektRestClient) = get("/person/inntekt") {
    val fnr = requireNotNull(call.request.header("ident")) { "Mangler header: [ident: fnr]" }
    val end = YearMonth.now().minusMonths(1)
    val start = end.minusMonths(11)
    val inntekterResult = inntektRestClient.hentInntektsliste(
        fnr = fnr,
        fom = start,
        tom = end,
        filter = "8-30",
        callId = UUID.randomUUID().toString()
    )
    when (inntekterResult) {
        is Result.Ok -> {
            val beregnetÅrsinntekt = inntekterResult.value.flatMap { it.inntektsliste }.sumOf { it.beløp }
            val beregnetMånedsinntekt = round(beregnetÅrsinntekt / 12)
            call.respond(
                mapOf(
                    "beregnetMånedsinntekt" to beregnetMånedsinntekt
                )
            )
        }
        is Result.Error -> call.respond(inntekterResult.error.statusCode, inntekterResult.error.response)
    }
}