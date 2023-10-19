package com.quixicon.presentation.activities.cards.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.io.Serializable

class BookCardModel(
    val id: Long,
    val original: String,
    val transcription: String?,
    val description: String?,
    val answer: String?,
    var size: Int = 0,
    var position: Int = 0,
    var knowledge: Int? = null,
    var showSpeaker: Boolean = false
) : BaseObservable(), Serializable {

    @Bindable
    val strPosition: String = "$position / $size"

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

    @Bindable
    fun getTranslationVisibility(): Int {
        return when {
            answer.isNullOrEmpty() -> View.GONE
            else -> View.VISIBLE
        }
    }

    @Bindable
    fun getSpeakerVisibility() = if (showSpeaker) View.VISIBLE else View.GONE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookCardModel

        if (original != other.original) return false
        if (transcription != other.transcription) return false
        if (description != other.description) return false
        if (answer != other.answer) return false
        if (knowledge != other.knowledge) return false
        return true
    }

    override fun hashCode(): Int {
        var result = original.hashCode()
        result = 31 * result + transcription.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + answer.hashCode()
        result = 31 * result + knowledge.hashCode()
        return result
    }
}
