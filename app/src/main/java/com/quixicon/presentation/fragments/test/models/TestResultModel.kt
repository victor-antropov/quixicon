package com.quixicon.presentation.fragments.test.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.io.Serializable

class TestResultModel(
    val correct: Int,
    val wrong: Int
) : BaseObservable(), Serializable {

    @Bindable
    fun getKnowledge(): Int {
        return if (correct == 0 && wrong == 0) 0 else correct * 100 / (correct + wrong)
    }
}
