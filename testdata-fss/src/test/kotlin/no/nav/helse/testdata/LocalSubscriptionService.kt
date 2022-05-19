package no.nav.helse.testdata

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.helse.testdata.api.Subscription

object LocalSubscriptionService : SubscriptionService {

    private val concreteSubscriptionService = ConcreteSubscriptionService

    override fun addSubscription(subscription: Subscription) {
        concreteSubscriptionService.addSubscription(subscription)

        runBlocking {
            delay(1500L)
            concreteSubscriptionService.update(subscription.fødselsnummer, "AVVENTER_VILKÅRSPRØVING")
            delay(350L)
            concreteSubscriptionService.update(subscription.fødselsnummer, "AVVENTER_SIMULERING")
            delay(650L)
            concreteSubscriptionService.update(subscription.fødselsnummer, "AVVENTER_GODKJENNING")
        }
    }

    override fun update(fødselsnummer: String, nyTilstand: String) {}
    override fun close(fødselsnummer: String) {}
}
