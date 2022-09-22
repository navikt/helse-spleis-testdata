package no.nav.helse.testdata.api

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import no.nav.helse.testdata.SubscriptionService
import no.nav.helse.testdata.objectMapper

internal data class SubscriptionFrame(
    val type: String,
    val fødselsnummer: String,
)

internal data class EndringFrame(
    val type: String,
    val tilstand: String,
)

data class Subscription(
    val session: WebSocketServerSession,
    val fødselsnummer: String,
)

internal fun Route.registerSubscriptionApi(subscriptionService: SubscriptionService) {
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
