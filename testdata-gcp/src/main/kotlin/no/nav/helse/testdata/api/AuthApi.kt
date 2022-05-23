package no.nav.helse.testdata.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import no.nav.helse.testdata.log

internal fun Route.registerAuthApi() {
    get("/oauth2/callback") {
        val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
        val accessToken = principal?.accessToken.toString()
        log.info(accessToken)
        call.sessions.set(UserSession(accessToken))
        call.respondRedirect("/")
    }
}

data class UserSession(val accessToken: String?) : Principal