package com.quixicon.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val map: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val exceptionText = "$modelClass class not found"
        val viewModelProvider = map[modelClass] ?: throw IllegalArgumentException(exceptionText)
        return viewModelProvider.get() as T
    }
}
