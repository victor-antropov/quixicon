package com.quixicon.background.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class DaggerWorkerFactory : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val constructor = Class.forName(workerClassName)
            .asSubclass(CoroutineWorker::class.java)
            .getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)

        return constructor.newInstance(appContext, workerParameters)
            .apply { inject(this) }
    }

    private fun inject(coroutineWorker: CoroutineWorker) {
        val app = coroutineWorker.applicationContext
        if (app !is HasCoroutineWorkerInjector) {
            val appName = app.javaClass.canonicalName
            val implName = HasCoroutineWorkerInjector::class.java.canonicalName
            throw RuntimeException("$appName does not implement $implName")
        }
        app.coroutineWorkerInjector().inject(coroutineWorker)
    }
}
