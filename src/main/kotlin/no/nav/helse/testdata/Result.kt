package no.nav.helse.testdata

import java.lang.Exception

abstract sealed class Result<T, E: Exception> {
    data class Ok<T, E: Exception>(val value: T) : Result<T, E>()
    data class Error<T, E: Exception>(val error: E) : Result<T, E> ()
}