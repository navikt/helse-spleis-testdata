package no.nav.helse.testdata

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.helse.testdata.api.EndringFrame
import no.nav.helse.testdata.api.Subscription

internal interface SubscriptionService {
    fun addSubscription(subscription: Subscription)
    fun update(fødselsnummer: String, nyTilstand: String)
    fun close(fødselsnummer: String)
}

internal object ConcreteSubscriptionService : SubscriptionService {
    private val subscriptions = mutableListOf<Subscription>()

    override fun addSubscription(subscription: Subscription) {
        subscriptions.add(subscription)
    }

    override fun update(fødselsnummer: String, nyTilstand: String) {
        val frameText = objectMapper.writeValueAsString(EndringFrame("endring", nyTilstand))
        var antallAbonnementerPåFnr: Int
        subscriptions.filter { it.fødselsnummer == fødselsnummer }
            .also { antallAbonnementerPåFnr = it.size }
            .map { it.session }
            .filter { it.isActive }
            .also { log.info("Sender oppdatering til ${it.size} aktive sesjoner, av totalt $antallAbonnementerPåFnr sesjoner for fnr.") }
            .forEach { session ->
                runBlocking {
                    launch {
                        session.outgoing.send(Frame.Text(frameText))
                    }
                }
            }
    }

    override fun close(fødselsnummer: String) {
        subscriptions.filter { it.fødselsnummer == fødselsnummer }.forEach {
            subscriptions.remove(it)
            runBlocking {
                launch {
                    it.session.close()
                }
            }
        }
    }
}
