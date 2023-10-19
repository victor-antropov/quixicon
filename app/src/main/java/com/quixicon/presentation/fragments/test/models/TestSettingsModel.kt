package com.quixicon.presentation.fragments.test.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.BR
import com.quixicon.domain.entities.enums.TestDirection
import com.quixicon.presentation.activities.test.models.TestCollectionModel
import java.io.Serializable

class TestSettingsModel(
    val collections: List<TestCollectionModel>,
    var selectedCollection: TestCollectionModel?,
    var testDirection: TestDirection,
    usePartDefault: Boolean,
    var showKnown: Boolean,
    var notShuffle: Boolean,
    var verticalSwipe: Boolean,
    var showTranscription: Boolean,
    var size: Int? = null,
    var volume: Int? = null,
    var playQuestion: Boolean
) : BaseObservable(), Serializable {

    @Bindable var usePartBlockVisibility = if (usePartDefault) View.VISIBLE else View.GONE

    @get:Bindable var usePart: Boolean = usePartDefault
        set(value) {
            field = value
            usePartBlockVisibility = if (value) View.VISIBLE else View.GONE
            notifyPropertyChanged(BR.usePart)
            notifyPropertyChanged(BR.usePartBlockVisibility)
        }
}
