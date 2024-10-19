package no.nav.helse.testdata.api

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import no.nav.helse.testdata.SubscriptionService
import no.nav.helse.testdata.log
import no.nav.helse.testdata.objectMapper
import java.util.*

internal class Oppdatering private constructor(val type: String, val verdi: String) {
    companion object {
        fun endring(tilstand: String) = Oppdatering("endring", tilstand)
        fun sletting(app: String) = Oppdatering("sletting", app)
    }
}

internal fun Routing.registerSubscriptionApi(sseService: SubscriptionService) {
    get("/sse/{fødselsnummer}") {
        val fødselsnummer = call.parameters["fødselsnummer"] ?: return@get call.respond(BadRequest)
        call.response.cacheControl(CacheControl.NoCache(null))
        val flow = sseService.addSubscription(fødselsnummer)
        call.respondBytesWriter(contentType = ContentType.Text.EventStream) {
            // wrap i coroutine for å kunne lukke ByteWriteChannel når klienten er borte
            launch {
                flow.collect { sendEndring(it, this) }
            }.join()
        }
    }
}

private suspend fun ByteWriteChannel.sendEndring(oppdatering: Oppdatering, coroutineScope: CoroutineScope) {
    if (isClosedForWrite) {
        log.info("Avbryter coroutine for lukket ByteWriteChannel")
        coroutineScope.cancel()
        return
    }
    writeStringUtf8("id: ${UUID.randomUUID()}\n")
    writeStringUtf8("event: tilstandsendring\n")
    writeStringUtf8("data: ${objectMapper.writeValueAsString(oppdatering)}\n")
    writeStringUtf8("\n")
    flush()
}
