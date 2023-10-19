package com.quixicon.presentation.fragments.draw.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.io.Serializable

class DrawModel(
    val title: String,
    val hintOriginal: String = "",
    val hintTranslation: String = ""
) : BaseObservable(), Serializable {

    @Bindable
    fun getOriginalVisibility() =
        if (hintOriginal.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

    @Bindable
    fun getTranslationVisibility() =
        if (hintTranslation.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
}
