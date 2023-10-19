package com.quixicon.presentation.activities.settings.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.interactors.GetCollectionsWithCardsInteractor
import com.quixicon.domain.interactors.base.BaseInteractor
import com.quixicon.domain.mappers.ListMapperImpl
import com.quixicon.presentation.activities.test.mappers.TestCollectionModelMapper
import com.quixicon.presentation.activities.test.models.TestCollectionModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val preferencesBoundary: PreferencesBoundary,
    private val getCollectionsWithCardsInteractor: GetCollectionsWithCardsInteractor,
    testCollectionMapper: TestCollectionModelMapper,
    app: Application
) : BaseViewModel(app) {

    override val interactors: List<BaseInteractor<*, *>> = listOf(
        getCollectionsWithCardsInteractor
    )

    private val collectionListMapper = ListMapperImpl(testCollectionMapper)

    private val _getCollectionsLiveData = PendingLiveData<EventArgs<List<TestCollectionModel>>>()
    val getCollectionsLiveData: LiveData<EventArgs<List<TestCollectionModel>>> = _getCollectionsLiveData

    var config = preferencesBoundary.getConfig()
        get() = preferencesBoundary.getConfig()
        set(value) {
            field = value
            preferencesBoundary.saveConfig(value)
        }

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun getCollections() {
        val params = GetCollectionsWithCardsInteractor.Params()
        getCollectionsWithCardsInteractor(params, ::onSubscribe, ::onFinally, ::onGetCollections, ::onError)
        interactor = getCollectionsWithCardsInteractor
    }

    private fun onGetCollections(collections: List<Pair<Collection, Int>>) {
        val testCollectionModels = collectionListMapper.mapToOutput(collections)
        _getCollectionsLiveData.value = EventArgs(testCollectionModels)
    }
}
