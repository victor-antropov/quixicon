package com.quixicon.presentation.activities.cards.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.R
import com.quixicon.domain.entities.enums.CardType
import java.io.Serializable

class CardModel(
    val id: Long,
    val name: String,
    val transcription: String,
    val description: String,
    val translation: String,
    val cardType: CardType,
    var knowledge: Int? = null,
    var copyFlag: Boolean = false,
    var subject: String? = null
) : BaseObservable(), Serializable {

    @Bindable
    fun getAccentColorAttribute(): Int {
        return when (cardType) {
            CardType.PHRASE -> R.attr.colorAccentPhrase
            CardType.RULE -> R.attr.colorAccentRule
            CardType.WORD -> R.attr.colorBackground
        }
    }

    @Bindable
    fun getCopyIconVisibility(): Int = if (copyFlag) View.VISIBLE else View.GONE

    @Bindable
    fun getSubjectCode() = subject ?: ""

    @Bindable
    fun getShowSubject(): Boolean = !(subject.isNullOrEmpty() || copyFlag)
}
