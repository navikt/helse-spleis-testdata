package no.nav.helse.testdata

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import no.nav.helse.testdata.api.Oppdatering
import no.nav.helse.testdata.api.Oppdatering.Companion.endring

internal object LocalSubscriptionService : SubscriptionService {

    private val concreteSubscriptionService = ConcreteSubscriptionService

    @OptIn(DelicateCoroutinesApi::class)
    override fun addSubscription(fødselsnummer: String): SharedFlow<Oppdatering> {
        return runBlocking {
            GlobalScope.launch {
                    delay(1500L)
                    update(fødselsnummer, endring("AVVENTER_VILKÅRSPRØVING"))
                    delay(350L)
                    update(fødselsnummer, endring("AVVENTER_SIMULERING"))
                    delay(650L)
                    update(fødselsnummer, endring("AVVENTER_GODKJENNING"))
            }
            concreteSubscriptionService.addSubscription(fødselsnummer)
        }
    }

    override fun update(fødselsnummer: String, oppdatering: Oppdatering) {
        concreteSubscriptionService.update(fødselsnummer, oppdatering)
    }
}
