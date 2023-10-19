package com.quixicon.presentation.views

import com.quixicon.core.system.EventArgs

interface AsyncOperationView {

    fun onAsyncOperationProgress(args: EventArgs<Boolean>) {
    }

    fun onAsyncOperationError(args: EventArgs<Throwable>)
}
