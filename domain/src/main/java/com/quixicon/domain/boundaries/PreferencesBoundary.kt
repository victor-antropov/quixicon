package com.quixicon.domain.boundaries

import com.quixicon.domain.entities.cache.QuixiconConfig

interface PreferencesBoundary {

    fun getConfig(): QuixiconConfig

    fun saveConfig(config: QuixiconConfig)

    fun getNotificationHint(): Boolean
    fun setNotificationHint(value: Boolean)
}
