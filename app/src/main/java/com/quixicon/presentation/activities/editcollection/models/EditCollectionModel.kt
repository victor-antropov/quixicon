package com.quixicon.presentation.activities.editcollection.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.BR
import com.quixicon.domain.entities.enums.LanguageCode
import timber.log.Timber

class EditCollectionModel(
    val id: Long = 0,
    var name: String? = "",
    var description: String? = "",
    var isShowSubjectDefault: Boolean = false,
    var subject: LanguageCode = LanguageCode.UNDEFINED
) : BaseObservable() {

    @get:Bindable
    var isShowSubject: Boolean = isShowSubjectDefault
        set(value) {
            field = value
            Timber.e("Set subject visibility to $value")
            subjectVisibility = if (value) View.VISIBLE else View.GONE
            notifyPropertyChanged(BR.subjectVisibility)
        }

    @Bindable
    var subjectVisibility = if (isShowSubject) View.VISIBLE else View.GONE
}
