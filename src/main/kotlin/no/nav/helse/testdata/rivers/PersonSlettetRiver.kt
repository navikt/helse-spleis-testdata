package no.nav.helse.testdata.rivers

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.testdata.SubscriptionService
import no.nav.helse.testdata.api.Oppdatering

internal class PersonSlettetRiver(
    rapidsConnection: RapidsConnection,
    private val subscriptionService: SubscriptionService,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "person_slettet") }
            validate {
                it.requireKey("fødselsnummer")
                it.requireKey("system_participating_services")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val app = packet["system_participating_services"][0].let { it["service"].asText() }

        subscriptionService.update(fødselsnummer, Oppdatering.sletting(app))
    }
}
