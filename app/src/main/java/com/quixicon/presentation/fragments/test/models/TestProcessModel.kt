package com.quixicon.presentation.fragments.test.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.domain.entities.enums.TestDirection
import java.io.Serializable

class TestProcessModel(
    val verticalSwipe: Boolean,
    val testDirection: TestDirection,
    val size: Int,
    var position: Int = 0,
    var qntCorrect: Int = 0,
    var qntWrong: Int = 0,
    val showTranscription: Boolean,
    val playQuestions: Boolean,
    val showDrawButton: Boolean,
    val showSwipeHint: Boolean,
    var showDrawPreview: Boolean = false,
    val ttsAvailable: Boolean = false
) : BaseObservable(), Serializable {

    @Bindable
    fun getDrawVisibility(): Int {
        return if (showDrawButton && !showDrawPreview) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @Bindable
    fun getHintVisibility(): Int {
        return if (showSwipeHint && !showDrawPreview) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @Bindable
    fun getDrawHintVisibility(): Int {
        return if (showDrawButton && !verticalSwipe && !showDrawPreview) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @Bindable
    fun getDrawPreviewVisibility(): Int {
        return if (showDrawPreview) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}
