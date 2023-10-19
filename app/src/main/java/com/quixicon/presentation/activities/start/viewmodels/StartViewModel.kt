package com.quixicon.presentation.activities.start.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.interactors.ImportMultiCollectionsInteractor
import com.quixicon.presentation.viewmodels.BaseViewModel
import javax.inject.Inject

class StartViewModel @Inject constructor(
    private val importMultiCollectionsInteractor: ImportMultiCollectionsInteractor,
    private val preferencesBoundary: PreferencesBoundary,
    app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        importMultiCollectionsInteractor
    )

    var config = preferencesBoundary.getConfig()
        get() = preferencesBoundary.getConfig()
        set(value) {
            field = value
            preferencesBoundary.saveConfig(value)
        }

    private val _importMultiCollectionsLiveData = PendingLiveData<EventArgs<Int>>()
    val importMultiCollectionsLiveData: LiveData<EventArgs<Int>> = _importMultiCollectionsLiveData

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun importMultiCollections(urls: List<String>, subjectLanguage: LanguageCode?, studentLanguage: LanguageCode?) {
        val params = ImportMultiCollectionsInteractor.Params(urls, subjectLanguage, studentLanguage)
        importMultiCollectionsInteractor(params, ::onSubscribe, ::onFinally, ::onMultiCollectionsImported, ::onError)
        interactor = importMultiCollectionsInteractor
    }

    private fun onMultiCollectionsImported(ids: List<Long>) {
        _importMultiCollectionsLiveData.value = EventArgs(ids.size)
    }
}
