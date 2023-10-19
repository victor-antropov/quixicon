package com.quixicon.presentation.activities.editcollection.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.interactors.GetCollectionInteractor
import com.quixicon.domain.interactors.UpdateCollectionInteractor
import com.quixicon.presentation.activities.editcollection.mappers.EditCollectionModelMapper
import com.quixicon.presentation.activities.editcollection.models.EditCollectionModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class EditCollectionViewModel @Inject constructor(
    private val getCollectionInteractor: GetCollectionInteractor,
    private val updateCollectionInteractor: UpdateCollectionInteractor,
    private val editCollectionMapper: EditCollectionModelMapper,
    private val preferencesBoundary: PreferencesBoundary,
    app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        getCollectionInteractor,
        updateCollectionInteractor
    )

    var config = preferencesBoundary.getConfig()
        get() = preferencesBoundary.getConfig()
        set(value) {
            field = value
            preferencesBoundary.saveConfig(value)
        }

    private val _getCollectionLiveData = PendingLiveData<EventArgs<EditCollectionModel>>()
    val getCollectionLiveData: LiveData<EventArgs<EditCollectionModel>> = _getCollectionLiveData

    private val _updateCollectionLiveData = PendingLiveData<EventArgs<Unit>>()
    val updateCollectionLiveData: LiveData<EventArgs<Unit>> = _updateCollectionLiveData

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun getCollection(id: Long) {
        val params = GetCollectionInteractor.Params(id)
        getCollectionInteractor(params, ::onSubscribe, ::onFinally, ::onGetCollection, ::onError)
        interactor = getCollectionInteractor
    }

    private fun onGetCollection(collection: Collection) {
        val collectionEditModel = editCollectionMapper.mapToOutput(collection)
        _getCollectionLiveData.value = EventArgs(collectionEditModel)
    }

    fun updateCollection(collectionModel: EditCollectionModel) {
        val params = UpdateCollectionInteractor.Params(editCollectionMapper.mapToInput(collectionModel))
        updateCollectionInteractor(params, ::onSubscribe, ::onFinally, ::onUpdateCollection, ::onError)
        interactor = updateCollectionInteractor
    }

    private fun onUpdateCollection(unit: Unit) {
        Timber.e("Collection is updated")
        _updateCollectionLiveData.value = EventArgs(Unit)
    }
}
