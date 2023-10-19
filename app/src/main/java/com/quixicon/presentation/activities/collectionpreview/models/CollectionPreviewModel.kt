package com.quixicon.presentation.activities.collectionpreview.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.domain.entities.enums.CollectionType
import java.io.Serializable

class CollectionPreviewModel(
    val name: String,
    val description: String = "",
    val qntWords: Int = 0,
    val qntPhrases: Int = 0,
    val qntRules: Int = 0,
    val collectionType: CollectionType = CollectionType.DEFAULT,
    val installedCollectionId: Long = 0,
    val cards: List<CardPreviewModel> = arrayListOf()
) : BaseObservable(), Serializable {

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
    fun isInstalled(): Boolean = installedCollectionId != 0L

    @Bindable
    fun getNameVisibility(): Int = if (name.isNotEmpty()) View.VISIBLE else View.GONE
}
