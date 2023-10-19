package com.quixicon.presentation.activities.settings.models

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.BR
import java.io.Serializable

class SettingsModel(
    var isDarkMode: Boolean,
    val useNotificationsDefault: Boolean,
    val version: String,
    var isDrawOn: Boolean,
    var isShowGlobal: Boolean,
    var useFilterDefault: Boolean,
    val isMultiLanguages: Boolean
) : BaseObservable(), Serializable {

    @get:Bindable
    var isSourceFixed: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.sourceFixed)
        }

    @Bindable
    var useNotificationsBlockVisibility = if (useNotificationsDefault) View.VISIBLE else View.GONE

    @get:Bindable
    var useNotifications: Boolean = useNotificationsDefault
        set(value) {
            field = value
            useNotificationsBlockVisibility = if (value) View.VISIBLE else View.GONE
            notifyPropertyChanged(BR.useNotifications)
            notifyPropertyChanged(BR.useNotificationsBlockVisibility)
        }

    @Bindable
    var useFilterBlockVisibility = if (useFilterDefault) View.VISIBLE else View.GONE

    @Bindable
    fun getMultiOptionsVisibility() = if (isMultiLanguages) View.VISIBLE else View.GONE

    @get:Bindable
    var useFilter: Boolean = useFilterDefault
        set(value) {
            field = value
            useFilterBlockVisibility = if (value) View.VISIBLE else View.GONE
            notifyPropertyChanged(BR.useFilter)
            notifyPropertyChanged(BR.useFilterBlockVisibility)
        }
}
