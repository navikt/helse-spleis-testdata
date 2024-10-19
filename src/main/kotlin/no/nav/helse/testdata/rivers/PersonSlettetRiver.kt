package no.nav.helse.testdata.rivers

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.testdata.SubscriptionService
import no.nav.helse.testdata.api.Oppdatering

internal class PersonSlettetRiver(
    rapidsConnection: RapidsConnection,
    private val subscriptionService: SubscriptionService,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "person_slettet")
                it.requireKey("fødselsnummer")
                it.requireKey("system_participating_services")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val app = packet["system_participating_services"][0].let { it["service"].asText() }

        subscriptionService.update(fødselsnummer, Oppdatering.sletting(app))
    }
}
