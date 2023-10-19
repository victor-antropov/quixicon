package com.quixicon.presentation.activities.editcard.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.BR
import com.quixicon.R
import com.quixicon.domain.entities.enums.CardType

class EditCardModel(
    val id: Long = 0,
    var original: String = "",
    var transcription: String = "",
    var description: String = "",
    var translation: String = "",
    var cardType: CardType = CardType.WORD
) : BaseObservable(), View.OnClickListener {

    @Bindable var wordBtnState = cardType != CardType.WORD

    @Bindable var ruleBtnState = cardType != CardType.RULE

    @Bindable var phraseBtnState = cardType != CardType.PHRASE

    @get:Bindable var extraFields: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.extraFields)
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_word -> {
                cardType = CardType.WORD
            }
            R.id.btn_rule -> {
                cardType = CardType.RULE
            }
            R.id.btn_phrase -> {
                cardType = CardType.PHRASE
            }
        }
        wordBtnState = cardType != CardType.WORD
        ruleBtnState = cardType != CardType.RULE
        phraseBtnState = cardType != CardType.PHRASE
        notifyPropertyChanged(BR.wordBtnState)
        notifyPropertyChanged(BR.ruleBtnState)
        notifyPropertyChanged(BR.phraseBtnState)
    }
}
