package com.quixicon.presentation.activities.start.models

import com.quixicon.domain.entities.enums.LanguageCode

class LanguageSelectModel(
    val name: String,
    val value: LanguageCode?
) {
    override fun toString(): String {
        return name
    }
}
