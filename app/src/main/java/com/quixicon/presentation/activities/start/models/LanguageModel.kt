package com.quixicon.presentation.activities.start.models

import com.quixicon.domain.entities.enums.LanguageCode

class LanguageModel(
    val name: String,
    val value: LanguageCode
) {
    override fun toString(): String {
        return name
    }
}
