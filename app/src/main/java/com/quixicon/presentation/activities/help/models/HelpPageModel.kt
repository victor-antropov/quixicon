package com.quixicon.presentation.activities.help.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class HelpPageModel(
    val str: String,
    var size: Int = 0,
    var position: Int = 0
) : BaseObservable() {
    @Bindable
    val strPosition: String = "$position / $size"
}
