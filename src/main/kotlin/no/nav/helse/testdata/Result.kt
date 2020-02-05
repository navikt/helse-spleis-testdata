package no.nav.helse.testdata

import java.lang.Exception

sealed class Result<T, E: Exception> {
    data class Ok<T, E: Exception>(val value: T) : Result<T, E>()
    data class Error<T, E: Exception>(val error: E) : Result<T, E> ()

    companion object {
        fun <T, E: Exception> ok(value: T): Result<T, E> = Ok(value)
        fun <T, E: Exception> error(error: E): Result<T, E> = Error(error)
    }
}