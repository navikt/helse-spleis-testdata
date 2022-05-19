package no.nav.helse.testdata.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.server.routing.*
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
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