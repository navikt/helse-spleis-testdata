package no.nav.helse.testdata

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.helse.testdata.api.EndringFrame
import no.nav.helse.testdata.api.Subscription

internal interface SubscriptionService {
    fun addSubscription(subscription: Subscription)
    fun update(fødselsnummer: String, nyTilstand: String)
    fun close(fødselsnummer: String)
}

internal class ConcreteSubscriptionService : SubscriptionService {
    private val subscriptions = mutableListOf<Subscription>()

    override fun addSubscription(subscription: Subscription) {
        subscriptions.add(subscription)
    }

    override fun update(fødselsnummer: String, nyTilstand: String) {
        val frame = EndringFrame("endring", nyTilstand)
        val payload = Frame.Text(objectMapper.writeValueAsString(frame))
        subscriptions.first { it.fødselsnummer == fødselsnummer }.let {
            runBlocking {
                launch {
                    it.session.outgoing.send(payload)
                }
            }
        }
    }

    override fun close(fødselsnummer: String) {
        subscriptions.first { it.fødselsnummer == fødselsnummer }.let {
            subscriptions.remove(it)
            runBlocking {
                launch {
                    it.session.close()
                }
            }
        }
    }
}
