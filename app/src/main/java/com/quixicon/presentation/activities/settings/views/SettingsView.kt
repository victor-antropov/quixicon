package com.quixicon.presentation.activities.settings.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.test.models.TestCollectionModel

interface SettingsView {
    fun onBindCollections(args: EventArgs<List<TestCollectionModel>>)
}
