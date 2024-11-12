package no.nav.helse.testdata.rivers

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.testdata.SubscriptionService
import no.nav.helse.testdata.api.Oppdatering

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

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val tilstand = packet["gjeldendeTilstand"].asText()

        subscriptionService.update(fødselsnummer, Oppdatering.endring(tilstand))
    }

}
