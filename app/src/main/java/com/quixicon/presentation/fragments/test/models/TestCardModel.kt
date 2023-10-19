package com.quixicon.presentation.fragments.test.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.BR
import java.io.Serializable

class TestCardModel(
    val id: Long,
    val original: String,
    val transcription: String?,
    val description: String?,
    val answer: String?,
    size: Int,
    position: Int,
    val qntCorrect: Int,
    val qntWrong: Int,
    val reverseDirection: Boolean = false,
    private val showTranscription: Boolean = false,
    private val showDescription: Boolean = false,
    val ttsAvailable: Boolean = false
) : BaseObservable(), Serializable {

    @Bindable
    val strPosition: String = "$position / $size"

    @Bindable
    fun getQuestionOriginal() = if (reverseDirection) answer else original

    @Bindable
    fun getQuestionTranscriptionVisibility(): Int {
        return when {
            reverseDirection -> View.GONE
            transcription.isNullOrEmpty() -> View.GONE
            showTranscription -> View.VISIBLE
            else -> View.GONE
        }
    }

    @Bindable
    fun getQuestionDescriptionVisibility(): Int {
        return when {
            reverseDirection -> View.GONE
            description.isNullOrEmpty() -> View.GONE
            showDescription -> View.VISIBLE
            else -> View.GONE
        }
    }

    @Bindable
    fun getDescriptionVisibility(): Int {
        return when {
            description.isNullOrEmpty() -> View.GONE
            else -> View.VISIBLE
        }
    }

    @Bindable
    fun getTranscriptionVisibility(): Int {
        return when {
            transcription.isNullOrEmpty() -> View.GONE
            else -> View.VISIBLE
        }
    }

    @Bindable
    fun getTranscriptionFormatted(): String = "[$transcription]"

    @get:Bindable var opened: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.opened)
            speakerVisibility = if (ttsAvailable && (!reverseDirection || value)) View.VISIBLE else View.GONE
            notifyPropertyChanged(BR.speakerVisibility)
        }

    @Bindable
    var speakerVisibility = if (ttsAvailable && (!reverseDirection || opened)) View.VISIBLE else View.GONE
}
