package no.nav.helse.testdata.rivers

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.testdata.SubscriptionService
import no.nav.helse.testdata.api.Oppdatering
import no.nav.helse.testdata.objectMapper

internal class TrengerOpplysningerFraArbeidsgiverRiver(
    rapidsConnection: RapidsConnection,
    private val subscriptionService: SubscriptionService,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "trenger_opplysninger_fra_arbeidsgiver") }
            validate {
                it.requireKey("fødselsnummer", "vedtaksperiodeId", "organisasjonsnummer")
                it.requireArray("sykmeldingsperioder") {
                    it.requireKey("fom")
                    it.requireKey("tom")
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val fødselsnummer = packet["fødselsnummer"].asText()

        subscriptionService.update(fødselsnummer, Oppdatering.forespørsel(objectMapper.createObjectNode().apply {
            put("vedtaksperiodeId", packet["vedtaksperiodeId"].asText())
            put("organisasjonsnummer", packet["organisasjonsnummer"].asText())
            putArray("sykmeldingsperioder").apply {
                packet["sykmeldingsperioder"].forEach { sykmeldingsperiode ->
                    add(objectMapper.createObjectNode().apply {
                        put("fom", sykmeldingsperiode.path("fom").asText())
                        put("tom", sykmeldingsperiode.path("tom").asText())
                    })
                }
            }
        }))
    }
}
