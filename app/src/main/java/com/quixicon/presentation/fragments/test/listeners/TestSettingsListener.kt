package com.quixicon.presentation.fragments.test.listeners

import com.quixicon.presentation.fragments.test.models.TestSettingsModel

interface TestSettingsListener {
    fun onTestSettingsReady(model: TestSettingsModel)
}
