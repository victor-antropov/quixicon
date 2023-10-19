package com.quixicon.presentation.fragments.selectlanguagedialog.models

import com.quixicon.domain.entities.enums.LanguageCode

class SelectLanguageModel(
    val name: String,
    val value: LanguageCode?
) {
    override fun toString(): String {
        return name
    }
}
