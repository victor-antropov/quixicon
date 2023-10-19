package com.quixicon.background.workers

import androidx.work.CoroutineWorker
import dagger.android.AndroidInjector

interface HasCoroutineWorkerInjector {

    fun coroutineWorkerInjector(): AndroidInjector<CoroutineWorker>
}
