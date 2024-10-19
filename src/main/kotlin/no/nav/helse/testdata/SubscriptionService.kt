package no.nav.helse.testdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nav.helse.testdata.api.Oppdatering

internal interface SubscriptionService {
    fun addSubscription(fødselsnummer: String): SharedFlow<Oppdatering>
    fun update(fødselsnummer: String, oppdatering: Oppdatering)
}

internal object ConcreteSubscriptionService : SubscriptionService {
    private val subscriptions = mutableMapOf<String, MutableSharedFlow<Oppdatering>>()

    override fun addSubscription(fødselsnummer: String): SharedFlow<Oppdatering> {
        val flow = subscriptions.getOrPut(fødselsnummer) { MutableSharedFlow() }
        log.info("Har subscriptions for ${subscriptions.size} fødselsnumre")
        log.info("Subscription count for $fødselsnummer: ${flow.subscriptionCount.value}")
        return flow.asSharedFlow()
    }

    override fun update(fødselsnummer: String, oppdatering: Oppdatering) {
        subscriptions[fødselsnummer]?.let { flow ->
            CoroutineScope(Dispatchers.IO).launch {
                flow.emit(oppdatering)
            }
        }
    }
}
