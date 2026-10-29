package no.nav.helse.testdata

import io.opentelemetry.api.trace.Span
import java.time.Instant
import java.time.temporal.ChronoUnit

internal fun gjeldendeTraceUrl(): String? =
    Span
        .current()
        .spanContext
        .takeIf { it.isValid }
        ?.let { traceUrl(it.traceId, Instant.now(), System.getenv("GOOGLE_CLOUD_PROJECT") ?: "ingenting") }

internal fun traceUrl(
    traceId: String,
    tidspunkt: Instant,
    projectId: String,
): String {
    val start = tidspunkt.minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES)
    val slutt = tidspunkt.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES)
    return "https://console.cloud.google.com/logs/query;query=jsonPayload.trace_id%3D%22$traceId%22;cursorTimestamp=$tidspunkt;startTime=$start;endTime=$slutt?project=$projectId"
}
