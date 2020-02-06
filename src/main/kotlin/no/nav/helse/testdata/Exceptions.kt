package no.nav.helse.testdata

import io.ktor.http.HttpStatusCode

class ResponseFailure(val statusCode: HttpStatusCode, val response: String) :
    Exception("Failed to execute http call, responded with status code $statusCode")

class FunctionalFailure(message: String) : Exception(message)