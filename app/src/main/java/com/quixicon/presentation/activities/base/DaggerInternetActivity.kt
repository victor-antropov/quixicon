package com.quixicon.presentation.activities.base

import com.quixicon.core.support.extensions.replaceFragment
import com.quixicon.presentation.fragments.base.ConnectionErrorFragment
import dagger.android.support.DaggerAppCompatActivity

abstract class DaggerInternetActivity : DaggerAppCompatActivity() {

    abstract fun invokeLatestRequest()

    protected fun replaceConnectionErrorFragment(containerId: Int) {
        replaceFragment(containerId, ConnectionErrorFragment.createInstance(true), false)
    }

    protected fun replaceServerErrorFragment(containerId: Int) {
        replaceFragment(containerId, ConnectionErrorFragment.createInstance(true, isServerError = true), false)
    }
}
