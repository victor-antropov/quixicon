package com.quixicon.presentation.activities.importcollection.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.presentation.support.helpers.LanguageCodeHelper
import java.io.Serializable

class CollectionInfoModel(
    val name: String,
    val description: String = "",
    val cntWords: Int = 0,
    val cntPhrases: Int = 0,
    val cntRules: Int = 0,
    val imei: String? = null,
    val link: String? = null,
    var isInstalled: Boolean = false,
    val id: Long = 0,
    val codeSubject: LanguageCode = LanguageCode.UNDEFINED,
    val codeStudent: LanguageCode = LanguageCode.UNDEFINED
) : BaseObservable(), Serializable {

    @Bindable
    fun getDescriptionVisibility() = if (description.isEmpty()) View.GONE else View.VISIBLE

    @Bindable
    fun getDivider1Visibility(): Int {
        val condition = cntWords > 0 && (cntPhrases > 0 || cntRules > 0)
        return if (condition) View.VISIBLE else View.GONE
    }

    @Bindable
    fun getDivider2Visibility(): Int {
        val condition = cntPhrases > 0 && cntRules > 0
        return if (condition) View.VISIBLE else View.GONE
    }

    @Bindable
    fun getSubject(): String = codeSubject.value

    @Bindable
    fun getStudent(): String = codeStudent.value

    @Bindable
    fun getSubjectIcon(): Int {
        return LanguageCodeHelper.getIcon(codeSubject)
    }
}
