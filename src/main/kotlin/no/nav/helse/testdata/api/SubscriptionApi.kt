package no.nav.helse.testdata.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import no.nav.helse.testdata.SubscriptionService
import no.nav.helse.testdata.objectMapper

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class SubscriptionFrame(
    val type: String,
    val fødselsnummer: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class EndringFrame(
    val type: String,
    val tilstand: String,
)

data class Subscription(
    val session: WebSocketServerSession,
    val fødselsnummer: String,
)

internal fun Routing.registerSubscriptionApi(subscriptionService: SubscriptionService) {
    webSocket("/ws/vedtaksperiode") {
        for (frame in incoming) {
            frame.getSubscriptionFrame()?.also {
                subscriptionService.addSubscription(Subscription(this, it.fødselsnummer))
            }
        }
    }
}

private fun Frame.getSubscriptionFrame(): SubscriptionFrame? =
    if (this is Frame.Text) objectMapper.readTree(readText())?.let {
        SubscriptionFrame(
            type = it.get("type").asText(),
            fødselsnummer = it.get("fødselsnummer").asText()
        )
    } else null