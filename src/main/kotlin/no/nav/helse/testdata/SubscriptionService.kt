package no.nav.helse.testdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nav.helse.testdata.api.EndringFrame

internal interface SubscriptionService {
    fun addSubscription(fødselsnummer: String): SharedFlow<EndringFrame>
    fun update(fødselsnummer: String, nyTilstand: String)
}

internal object ConcreteSubscriptionService: SubscriptionService {
    private val subscriptions = mutableMapOf<String, MutableSharedFlow<EndringFrame>>()
    override fun addSubscription(fødselsnummer: String): SharedFlow<EndringFrame> {
        val flow = subscriptions.getOrPut(fødselsnummer) { MutableSharedFlow() }
        return flow.asSharedFlow()
    }

    override fun update(fødselsnummer: String, nyTilstand: String) {
        subscriptions[fødselsnummer]?.let { flow ->
            CoroutineScope(Dispatchers.IO).launch {
                flow.emit(EndringFrame("endring", nyTilstand))
            }
        }
    }
}
