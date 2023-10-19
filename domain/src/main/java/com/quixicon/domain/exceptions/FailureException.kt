package com.quixicon.domain.exceptions

class FailureException(val failure: Failure, cause: Throwable? = null) : Throwable(cause)
