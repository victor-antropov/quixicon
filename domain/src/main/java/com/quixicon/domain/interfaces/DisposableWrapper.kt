package com.quixicon.domain.interfaces

interface DisposableWrapper {

    fun isDisposed(): Boolean

    fun dispose()
}
