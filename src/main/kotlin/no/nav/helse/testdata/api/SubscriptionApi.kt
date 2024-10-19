package no.nav.helse.testdata.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.server.application.call
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
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

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class EndringFrame(
    val type: String,
    val tilstand: String,
)

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

private suspend fun ByteWriteChannel.sendEndring(endring: EndringFrame, coroutineScope: CoroutineScope) {
    if (isClosedForWrite) {
        log.info("Avbryter coroutine for lukket ByteWriteChannel")
        coroutineScope.cancel()
        return
    }
    writeStringUtf8("id: ${UUID.randomUUID()}\n")
    writeStringUtf8("event: tilstandsendring\n")
    writeStringUtf8("data: ${objectMapper.writeValueAsString(endring)}\n")
    writeStringUtf8("\n")
    flush()
}
