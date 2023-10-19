package com.quixicon.presentation.activities.settings.models

import com.quixicon.domain.entities.enums.NotificationSource

class NotificationSourceModel(
    val name: String,
    val value: NotificationSource
) {

    override fun toString(): String {
        return name
    }
}
