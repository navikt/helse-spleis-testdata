package no.nav.helse.testdata

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import no.nav.helse.testdata.api.EndringFrame

internal object LocalSubscriptionService : SubscriptionService {

    private val concreteSubscriptionService = ConcreteSubscriptionService

    @OptIn(DelicateCoroutinesApi::class)
    override fun addSubscription(fødselsnummer: String): SharedFlow<EndringFrame> {
        return runBlocking {
            GlobalScope.launch {
                    delay(1500L)
                    update(fødselsnummer, "AVVENTER_VILKÅRSPRØVING")
                    delay(350L)
                    update(fødselsnummer, "AVVENTER_SIMULERING")
                    delay(650L)
                    update(fødselsnummer, "AVVENTER_GODKJENNING")
            }
            concreteSubscriptionService.addSubscription(fødselsnummer)
        }
    }

    override fun update(fødselsnummer: String, nyTilstand: String) {
        concreteSubscriptionService.update(fødselsnummer, nyTilstand)
    }
}