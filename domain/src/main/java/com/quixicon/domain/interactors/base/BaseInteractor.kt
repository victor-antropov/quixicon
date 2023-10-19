package com.quixicon.domain.interactors.base

import androidx.annotation.VisibleForTesting
import com.quixicon.domain.interfaces.DisposableWrapper
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseInteractor<T, Params>(
    private val scheduler: Scheduler,
    private val postScheduler: Scheduler
) : DisposableWrapper {

    private val compositeDisposable = CompositeDisposable()

    private var params: Params? = null
    private var doOnSubscribe: ((Disposable) -> Unit)? = null
    private var doOnFinally: (() -> Unit)? = null
    private var onSuccess: ((T) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    @VisibleForTesting
    abstract fun createSingle(params: Params): Single<T>

    override fun isDisposed(): Boolean {
        return compositeDisposable.isDisposed
    }

    override fun dispose() {
        if (!isDisposed()) compositeDisposable.dispose()
        params = null
        doOnSubscribe = null
        doOnFinally = null
        onSuccess = null
        onError = null
    }

    operator fun invoke(
        params: Params,
        doOnSubscribe: ((Disposable) -> Unit)?,
        doOnFinally: (() -> Unit)?,
        onSuccess: ((T) -> Unit)?,
        onError: ((Throwable) -> Unit)?,
        scheduler: Scheduler? = null,
        postScheduler: Scheduler? = null
    ) {
        this.params = params
        this.doOnSubscribe = doOnSubscribe
        this.doOnFinally = doOnFinally
        this.onSuccess = onSuccess
        this.onError = onError

        invokeLatestRequest(scheduler, postScheduler)
    }

    fun invokeLatestRequest(scheduler: Scheduler? = null, postScheduler: Scheduler? = null) {
        params?.let {
            compositeDisposable.add(
                createSingle(it)
                    .subscribeOn(scheduler ?: this.scheduler)
                    .observeOn(postScheduler ?: this.postScheduler)
                    .doOnSubscribe(doOnSubscribe)
                    .doFinally(doOnFinally)
                    .subscribe(onSuccess, onError)
            )
        }
    }
}
