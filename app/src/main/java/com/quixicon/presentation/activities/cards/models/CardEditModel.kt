package com.quixicon.presentation.activities.cards.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.presentation.activities.cards.entities.CardSwipeMode
import java.io.Serializable

class CardEditModel(
    private val swipeMode: CardSwipeMode = CardSwipeMode.DISABLED,
    val showSubject: Boolean = false
) : BaseObservable(), Serializable {

    @Bindable
    fun getSwipeRightElementsVisibility(): Int = if (swipeMode != CardSwipeMode.DISABLED) View.VISIBLE else View.GONE

    @Bindable
    fun getSwipeLeftElementsVisibility(): Int = if (swipeMode == CardSwipeMode.ENABLED) View.VISIBLE else View.GONE
}
