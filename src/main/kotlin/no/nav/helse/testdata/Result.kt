package no.nav.helse.testdata

import java.lang.Exception

abstract sealed class Result<T, E: Exception> {
    abstract fun unwrap():T
    data class Ok<T, E: Exception>(val value: T) : Result<T, E>() {
        override fun unwrap() = value
    }
    data class Error<T, E: Exception>(val error: E) : Result<T, E> () {
        override fun unwrap() = throw UnwrapException(error)
    }
}

class UnwrapException(exception: Exception): Exception(exception)