package no.nav.helse.testdata.rivers

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.testdata.SubscriptionService

internal class VedtaksperiodeEndretRiver(
    rapidsConnection: RapidsConnection,
    private val subscriptionService: SubscriptionService,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "vedtaksperiode_endret")
                it.requireKey("vedtaksperiodeId")
                it.requireKey("fødselsnummer")
                it.requireKey("@id")
                it.requireKey("gjeldendeTilstand")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val tilstand = packet["gjeldendeTilstand"].asText()

        subscriptionService.update(fødselsnummer, tilstand)
    }

}