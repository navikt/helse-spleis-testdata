package no.nav.helse.testdata

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.helse.testdata.api.EndringFrame
import no.nav.helse.testdata.api.Subscription

class LocalSubscriptionService() : SubscriptionService {
    override fun addSubscription(subscription: Subscription) {
        subscription.session.let { session ->
            runBlocking {
                launch {
                    session.sendPayload("AVVENTER_VILKÅRSPRØVING".toEndringPayload())
                    session.sendPayload("AVVENTER_SIMULERING".toEndringPayload())
                    session.sendPayload("AVVENTER_GODKJENNING".toEndringPayload())
                    session.close()
                }
            }
        }
    }

    override fun update(fødselsnummer: String, nyTilstand: String) {}

    override fun close(fødselsnummer: String) {}

    private suspend fun WebSocketSession.sendPayload(payload: Frame) {
        delay(2000L)
        this.outgoing.send(payload)
    }

    private fun String.toEndringPayload() =
        Frame.Text(objectMapper.writeValueAsString(EndringFrame("endring", this)))

}