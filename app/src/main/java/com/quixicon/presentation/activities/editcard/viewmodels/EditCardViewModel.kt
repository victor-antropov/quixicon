package com.quixicon.presentation.activities.editcard.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.interactors.GetCardInteractor
import com.quixicon.domain.interactors.UpdateCardInteractor
import com.quixicon.presentation.activities.editcard.mappers.EditCardModelMapper
import com.quixicon.presentation.activities.editcard.models.EditCardModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class EditCardViewModel @Inject constructor(
    private val getCardInteractor: GetCardInteractor,
    private val updateCardInteractor: UpdateCardInteractor,
    val editCardMapper: EditCardModelMapper,
    app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        getCardInteractor,
        updateCardInteractor
    )

    private val _getCardLiveData = PendingLiveData<EventArgs<EditCardModel>>()
    val getCardLiveData: LiveData<EventArgs<EditCardModel>> = _getCardLiveData

    private val _updateCardLiveData = PendingLiveData<EventArgs<Unit>>()
    val updateCardLiveData: LiveData<EventArgs<Unit>> = _updateCardLiveData

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun getCard(id: Long) {
        val params = GetCardInteractor.Params(id)
        getCardInteractor(params, ::onSubscribe, ::onFinally, ::onGetCard, ::onError)
        interactor = getCardInteractor
    }

    private fun onGetCard(card: Card) {
        val cardEditModel = editCardMapper.mapToOutput(card)
        _getCardLiveData.value = EventArgs(cardEditModel)
    }

    fun updateCard(cardModel: EditCardModel, collectionId: Long? = null) {
        val params = UpdateCardInteractor.Params(editCardMapper.mapToInput(cardModel), collectionId)
        updateCardInteractor(params, ::onSubscribe, ::onFinally, ::onUpdateCard, ::onError)
        interactor = updateCardInteractor
    }

    private fun onUpdateCard(unit: Unit) {
        _updateCardLiveData.value = EventArgs(Unit)
    }
}
