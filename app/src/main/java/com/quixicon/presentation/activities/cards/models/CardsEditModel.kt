package com.quixicon.presentation.activities.cards.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.BR
import java.io.Serializable

class CardsEditModel(
    val swipeModeDefault: Boolean = false
) : BaseObservable(), Serializable {

    @Bindable var swipeBlockVisibility: Int = if (swipeModeDefault) View.VISIBLE else View.GONE

    @get:Bindable var swipeMode: Boolean = swipeModeDefault
        set(value) {
            field = value
            swipeBlockVisibility = if (value) View.VISIBLE else View.GONE
            notifyPropertyChanged(BR.swipeMode)
            notifyPropertyChanged(BR.swipeBlockVisibility)
        }

    @get:Bindable var leftActionName: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.leftActionName)
        }

    @get:Bindable var rightActionName: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.rightActionName)
        }

    @get:Bindable var isEmpty: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.empty)
        }
}
