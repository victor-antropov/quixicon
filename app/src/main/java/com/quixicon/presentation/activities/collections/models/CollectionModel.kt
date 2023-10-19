package com.quixicon.presentation.activities.collections.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.R
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import java.io.Serializable

class CollectionModel(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val qntWords: Int = 0,
    val qntPhrases: Int = 0,
    val qntRules: Int = 0,
    val superType: CardType? = null,
    val collectionType: CollectionType = CollectionType.DEFAULT,
    val subject: LanguageCode? = null,
    val showSubject: Boolean = false
) : BaseObservable(), Serializable {

    @Bindable
    fun getStrokeColorAttribute(): Int {
        return when (superType) {
            CardType.WORD -> R.attr.colorAccentWord
            CardType.PHRASE -> R.attr.colorAccentPhrase
            CardType.RULE -> R.attr.colorAccentRule
            null -> android.R.attr.colorBackground
        }
    }

    @Bindable
    fun getDescriptionVisibility() = if (description.isEmpty()) View.GONE else View.VISIBLE

    fun size() = qntPhrases + qntRules + qntWords

    @Bindable
    fun getDivider1Visibility(): Int {
        val condition = qntWords > 0 && (qntPhrases > 0 || qntRules > 0)
        return if (condition) View.VISIBLE else View.GONE
    }

    @Bindable
    fun getDivider2Visibility(): Int {
        val condition = qntPhrases > 0 && qntRules > 0
        return if (condition) View.VISIBLE else View.GONE
    }

    @Bindable
    fun getIconVisibility(): Int {
        return if (superType == null) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @Bindable
    fun getSubjectVisibility(): Int = if (!showSubject || subject == null || subject.value.isEmpty() || superType != null) View.GONE else View.VISIBLE

    @Bindable
    fun getSubjectCode(): String? = subject?.value
}
