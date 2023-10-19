package com.quixicon.presentation.activities.collectionpreview.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.R
import com.quixicon.domain.entities.enums.CardType
import java.io.Serializable

class CardPreviewModel(
    val name: String,
    val transcription: String,
    val description: String,
    val translation: String,
    val cardType: CardType
) : BaseObservable(), Serializable {

    @Bindable
    fun getAccentColorAttribute(): Int {
        return when (cardType) {
            CardType.PHRASE -> R.attr.colorAccentPhrase
            CardType.RULE -> R.attr.colorAccentRule
            CardType.WORD -> R.attr.colorBackground
        }
    }
}
