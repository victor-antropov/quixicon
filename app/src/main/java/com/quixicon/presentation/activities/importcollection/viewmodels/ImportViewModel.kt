package com.quixicon.presentation.activities.importcollection.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.R
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.enums.CollectionPriceCategory
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionInfo
import com.quixicon.domain.interactors.DeleteCollectionInteractor
import com.quixicon.domain.interactors.GetRemoteCollectionsInteractor
import com.quixicon.domain.interactors.ImportCollectionInteractor
import com.quixicon.domain.mappers.ListMapperImpl
import com.quixicon.presentation.activities.importcollection.mappers.CollectionInfoModelMapper
import com.quixicon.presentation.activities.importcollection.models.CollectionInfoModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class ImportViewModel @Inject constructor(
    private val deleteCollectionInteractor: DeleteCollectionInteractor,
    private val getRemoteCollectionsInteractor: GetRemoteCollectionsInteractor,
    private val importCollectionInteractor: ImportCollectionInteractor,
    private val preferencesBoundary: PreferencesBoundary,
    collectionMapper: CollectionInfoModelMapper,
    val app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        getRemoteCollectionsInteractor,
        importCollectionInteractor,
        deleteCollectionInteractor
    )

    var config = preferencesBoundary.getConfig()
        get() = preferencesBoundary.getConfig()
        set(value) {
            if (field != value) {
                field = value
                preferencesBoundary.saveConfig(value)
            }
        }

    val isMultiLanguages = !app.baseContext.resources.getBoolean(R.bool.use_only_language)

    private val collectionsListMapper = ListMapperImpl(collectionMapper)

    private val _getCollectionsLiveData = PendingLiveData<EventArgs<List<CollectionInfoModel>>>()
    val getCollectionsLiveData: LiveData<EventArgs<List<CollectionInfoModel>>> = _getCollectionsLiveData

    private val _importCollectionLiveData = PendingLiveData<EventArgs<Int>>()
    val importCollectionLiveData: LiveData<EventArgs<Int>> = _importCollectionLiveData

    private val _getCollectionDeletedLiveData = PendingLiveData<EventArgs<Unit>>()
    val getCollectionDeletedLiveData: LiveData<EventArgs<Unit>> = _getCollectionDeletedLiveData

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun importCollection(subject: LanguageCode, student: LanguageCode, url: String) {
        val params = ImportCollectionInteractor.Params(
            subject = subject,
            student = student,
            url = url,
            isLocal = false
        )
        importCollectionInteractor(params, ::onSubscribe, ::onFinally, ::onCollectionImported, ::onError)
        interactor = importCollectionInteractor
    }

    private fun onCollectionImported(id: Long) {
        _importCollectionLiveData.value = EventArgs(1)
    }

    fun getRemoteCollections(category: CollectionPriceCategory, filterSubject: LanguageCode?, filterStudent: LanguageCode?) {
        Timber.e("Get collections: $filterSubject, $filterStudent, $category")

        val params = GetRemoteCollectionsInteractor.Params(
            filterSubject = filterSubject,
            filterStudent = filterStudent,
            category = category,
            isMultiLanguages = isMultiLanguages
        )

        getRemoteCollectionsInteractor(params, ::onSubscribe, ::onFinally, ::onGetCollectionsInfo, ::onError)
        interactor = getRemoteCollectionsInteractor
    }

    private fun onGetCollectionsInfo(collections: List<Pair<CollectionInfo, Long>>) {
        Timber.e("Remote Collections: $collections")
        val collectionModels = collectionsListMapper.mapToOutput(collections).sortedWith(compareBy({ it.codeSubject }, { it.codeStudent }))
        _getCollectionsLiveData.value = EventArgs(collectionModels)
    }

    fun deleteCollection(collectionId: Long) {
        val params = DeleteCollectionInteractor.Params(collectionId)
        deleteCollectionInteractor(params, ::onSubscribe, ::onFinally, ::onDeleteCollection, ::onError)
        interactor = deleteCollectionInteractor
    }

    private fun onDeleteCollection(result: Unit) {
        _getCollectionDeletedLiveData.value = EventArgs(result)
    }
}
