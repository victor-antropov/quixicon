package com.quixicon.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.interactors.base.BaseInteractor
import com.quixicon.domain.interfaces.DisposableWrapper
import io.reactivex.disposables.Disposable

abstract class BaseViewModel(app: Application) : AndroidViewModel(app), DisposableWrapper {

    protected abstract val interactors: List<BaseInteractor<*, *>>

    protected var interactor: BaseInteractor<*, *>? = null

    private val _progressLiveData = PendingLiveData<EventArgs<Boolean>>()
    val progressLiveData: LiveData<EventArgs<Boolean>> = _progressLiveData

    private val _errorLiveData = PendingLiveData<EventArgs<Throwable>>()
    val errorLiveData: LiveData<EventArgs<Throwable>> = _errorLiveData

    override fun isDisposed(): Boolean {
        return interactors.all { it.isDisposed() }
    }

    override fun dispose() {
        if (!isDisposed()) interactors.forEach { it.dispose() }
        interactor = null
    }

    protected fun onSubscribe(@Suppress("UNUSED_PARAMETER") disposable: Disposable) {
        _progressLiveData.value = EventArgs(true)
    }

    protected fun onFinally() {
        _progressLiveData.value = EventArgs(false)
    }

    protected fun onError(e: Throwable) {
        _errorLiveData.value = EventArgs(e)
    }
}
