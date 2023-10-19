package com.quixicon.presentation.activities.collectionpreview.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.R
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionData
import com.quixicon.domain.interactors.GetRemoteCollectionMapInteractor
import com.quixicon.domain.interactors.ImportCollectionFromDataInteractor
import com.quixicon.presentation.activities.collectionpreview.mappers.CollectionPreviewModelMapper
import com.quixicon.presentation.activities.collectionpreview.models.CollectionPreviewModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class CollectionPreviewViewModel @Inject constructor(
    private val getRemoteCollectionMapInteractor: GetRemoteCollectionMapInteractor,
    private val importCollectionFromDataInteractor: ImportCollectionFromDataInteractor,
    private val collectionPreviewMapper: CollectionPreviewModelMapper,
    private val preferencesBoundary: PreferencesBoundary,
    val app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        getRemoteCollectionMapInteractor,
        importCollectionFromDataInteractor
    )

    var config = preferencesBoundary.getConfig()
        get() = preferencesBoundary.getConfig()
        set(value) {
            if (field != value) {
                field = value
                preferencesBoundary.saveConfig(value)
            }
        }

    private val _getRemoteCollectionLiveData = PendingLiveData<EventArgs<CollectionPreviewModel>>()
    val getRemoteCollectionLiveData: LiveData<EventArgs<CollectionPreviewModel>> = _getRemoteCollectionLiveData

    private val _importCollectionLiveData = PendingLiveData<EventArgs<Int>>()
    val importCollectionLiveData: LiveData<EventArgs<Int>> = _importCollectionLiveData

    private var collectionData: CollectionData? = null

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun importThisCollection() {
        collectionData?.let {
            val params = ImportCollectionFromDataInteractor.Params(it)
            importCollectionFromDataInteractor(params, ::onSubscribe, ::onFinally, ::onCollectionImported, ::onError)
            interactor = importCollectionFromDataInteractor
        }
    }

    private fun onCollectionImported(id: Long) {
        Timber.e("Collection Imported!!! : $id")
        _importCollectionLiveData.value = EventArgs(1)
    }

    fun getRemoteCollection(subject: LanguageCode, student: LanguageCode, id: String) {
        val isMultiLanguage = !app.baseContext.resources.getBoolean(R.bool.use_only_language)
        val params = GetRemoteCollectionMapInteractor.Params(subject, student, id, isMultiLanguage)
        getRemoteCollectionMapInteractor(params, ::onSubscribe, ::onFinally, ::onGetRemoteCollection, ::onError)
        interactor = getRemoteCollectionMapInteractor
    }

    private fun onGetRemoteCollection(collection: Pair<CollectionData, Long>) {
        collectionData = collection.first
        _getRemoteCollectionLiveData.value = EventArgs(collectionPreviewMapper.mapToOutput(collection))
    }
}
