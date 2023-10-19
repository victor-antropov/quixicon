package com.quixicon.domain.exceptions

import com.quixicon.domain.entities.error.ErrorDetails

sealed class Failure {

    object ConnectionError : Failure()
    object IoError : Failure()

    open class ServerError(val code: Int, val details: ErrorDetails) : Failure()
    class UnknownServerError(code: Int, details: ErrorDetails) : ServerError(code, details)
}
