package com.quixicon.presentation.activities.cards.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.interactors.CopyCardInteractor
import com.quixicon.domain.interactors.DeleteCardInteractor
import com.quixicon.domain.interactors.GetCardsInteractor
import com.quixicon.domain.interactors.GetLinkedCollectionsInteractor
import com.quixicon.domain.interactors.GetOtherCollectionsInteractor
import com.quixicon.domain.interactors.UpdateCardKnowledgeInteractor
import com.quixicon.domain.interactors.UpdateCollectionDateInteractor
import com.quixicon.domain.mappers.ListMapperImpl
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.activities.cards.mappers.CardActionMapper
import com.quixicon.presentation.activities.cards.mappers.CardModelMapper
import com.quixicon.presentation.activities.cards.models.BookCardModel
import com.quixicon.presentation.activities.cards.models.CardModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import javax.inject.Inject

class CardsViewModel @Inject constructor(
    private val getCardsInteractor: GetCardsInteractor,
    private val getOtherCollectionsInteractor: GetOtherCollectionsInteractor,
    private val updateCardKnowledgeInteractor: UpdateCardKnowledgeInteractor,
    private val copyCardInteractor: CopyCardInteractor,
    private val deleteCardInteractor: DeleteCardInteractor,
    private val getLinkedCollectionsInteractor: GetLinkedCollectionsInteractor,
    private val updateCollectionDateInteractor: UpdateCollectionDateInteractor,
    private val preferencesBoundary: PreferencesBoundary,
    cardsMapper: CardModelMapper,
    cardActionMapper: CardActionMapper,
    val app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        getCardsInteractor,
        getOtherCollectionsInteractor,
        updateCardKnowledgeInteractor,
        copyCardInteractor,
        deleteCardInteractor,
        getLinkedCollectionsInteractor,
        updateCollectionDateInteractor
    )

    var config = preferencesBoundary.getConfig()
        get() = preferencesBoundary.getConfig()
        set(value) {
            if (field != value) {
                field = value
                preferencesBoundary.saveConfig(value)
            }
        }

    private val cardsListMapper = ListMapperImpl(cardsMapper)
    private val collectionsListMapper = ListMapperImpl(cardActionMapper)

    private val _getCardsLiveData = PendingLiveData<EventArgs<List<CardModel>>>()
    val getCardsLiveData: LiveData<EventArgs<List<CardModel>>> = _getCardsLiveData

    private val _getBookCardsLiveData = PendingLiveData<EventArgs<List<BookCardModel>>>()
    val getBookCardsLiveData: LiveData<EventArgs<List<BookCardModel>>> = _getBookCardsLiveData

    private val _getOtherCollectionsLiveData = PendingLiveData<EventArgs<List<CardAction>>>()
    val getOtherCollectionsLiveData: LiveData<EventArgs<List<CardAction>>> = _getOtherCollectionsLiveData

    private val _getCardCopiedLiveData = PendingLiveData<EventArgs<Unit>>()
    val getCardCopiedLiveData: LiveData<EventArgs<Unit>> = _getCardCopiedLiveData

    private val _getCardDeletedLiveData = PendingLiveData<EventArgs<Unit>>()
    val getCardDeletedLiveData: LiveData<EventArgs<Unit>> = _getCardDeletedLiveData

    private val _getCardUpdatedLiveData = PendingLiveData<EventArgs<Unit>>()
    val getCardUpdateLiveData: LiveData<EventArgs<Unit>> = _getCardUpdatedLiveData

    private val _getLinkedCollectionsLiveData = PendingLiveData<EventArgs<List<String>>>()
    val getLinkedCollectionsLiveData: LiveData<EventArgs<List<String>>> = _getLinkedCollectionsLiveData

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun getCards(id: Long, order: CardSortOrder, filter: LanguageCode? = null) {
        val params = GetCardsInteractor.Params(id, order = order, subjectFilter = filter)
        getCardsInteractor(params, ::onSubscribe, ::onFinally, ::onGetCards, ::onError)
        interactor = getCardsInteractor
    }

    fun getCards(type: CardType, order: CardSortOrder, filter: LanguageCode? = null) {
        val params = GetCardsInteractor.Params(superType = type, order = order, subjectFilter = filter)
        getCardsInteractor(params, ::onSubscribe, ::onFinally, ::onGetCards, ::onError)
        interactor = getCardsInteractor
    }

    fun getAllCards(order: CardSortOrder, filter: LanguageCode? = null) {
        val params = GetCardsInteractor.Params(order = order, subjectFilter = filter)
        getCardsInteractor(params, ::onSubscribe, ::onFinally, ::onGetCards, ::onError)
        interactor = getCardsInteractor
    }

    private fun onGetCards(cards: List<Card>) {
        val cardModels = cardsListMapper.mapToOutput(cards)
        _getCardsLiveData.value = EventArgs(cardModels)
    }

    fun getBookCards(id: Long, order: CardSortOrder, filter: LanguageCode? = null) {
        val params = GetCardsInteractor.Params(id, order = order, subjectFilter = filter)
        getCardsInteractor(params, ::onSubscribe, ::onFinally, ::onGetBookCards, ::onError)
        interactor = getCardsInteractor
    }

    fun getBookCards(type: CardType, order: CardSortOrder, filter: LanguageCode? = null) {
        val params = GetCardsInteractor.Params(superType = type, order = order, subjectFilter = filter)
        getCardsInteractor(params, ::onSubscribe, ::onFinally, ::onGetBookCards, ::onError)
        interactor = getCardsInteractor
    }

    private fun onGetBookCards(cards: List<Card>) {
        val cardModels = cards.mapIndexed { index, card ->
            BookCardModel(
                id = card.id,
                original = card.name ?: "",
                transcription = card.transcription,
                description = card.extraData?.description,
                answer = card.translation ?: "",
                knowledge = card.knowledge,
                size = cards.size,
                position = index + 1
            )
        }

        _getBookCardsLiveData.value = EventArgs(cardModels)
    }

    fun getOtherCollections(id: Long) {
        val filter = if (config.useFilter) config.languageSubject else null
        val params = GetOtherCollectionsInteractor.Params(id, filter)
        getOtherCollectionsInteractor(params, ::onSubscribe, ::onFinally, ::onGetOtherCollections, ::onError)
        interactor = getOtherCollectionsInteractor
    }

    private fun onGetOtherCollections(collections: List<Collection>) {
        val collectionModels = collectionsListMapper.mapToOutput(collections)
        _getOtherCollectionsLiveData.value = EventArgs(collectionModels)
    }

    fun processAction(cardId: Long, cardAction: CardAction) {
        when (cardAction.action) {
            CardActionType.COPY -> {
                cardAction.id?.let {
                    copyCard(cardId, it)
                }
            }
            CardActionType.SET_100 -> {
                updateCardKnowledge(cardId, 100)
            }
            CardActionType.SET_0 -> {
                updateCardKnowledge(cardId, 0)
            }
            CardActionType.DELETE -> {
                cardAction.id?.let {
                    deleteCard(cardId, it)
                }
            }
            else -> {}
        }
    }

    fun updateCardKnowledge(id: Long, value: Int) {
        val params = UpdateCardKnowledgeInteractor.Params(id, value)
        updateCardKnowledgeInteractor(params, ::onSubscribe, ::onFinally, ::onUpdateCard, ::onError)
        interactor = updateCardKnowledgeInteractor
    }

    private fun onUpdateCard(result: Int) {
        _getCardUpdatedLiveData.value = EventArgs(Unit)
    }

    fun copyCard(cardId: Long, collectionId: Long) {
        val params = CopyCardInteractor.Params(cardId, collectionId)
        copyCardInteractor(params, ::onSubscribe, ::onFinally, ::onCopyCard, ::onError)
        interactor = copyCardInteractor
    }

    private fun onCopyCard(result: Long) {
        _getCardCopiedLiveData.value = EventArgs(Unit)
    }

    fun deleteCard(cardId: Long, collectionId: Long?) {
        val params = DeleteCardInteractor.Params(cardId, collectionId)
        deleteCardInteractor(params, ::onSubscribe, ::onFinally, ::onDeleteCard, ::onError)
        interactor = deleteCardInteractor
    }

    private fun onDeleteCard(result: Unit) {
        _getCardDeletedLiveData.value = EventArgs(Unit)
    }

    fun getLinkedCollections(cardId: Long) {
        val params = GetLinkedCollectionsInteractor.Params(cardId)
        getLinkedCollectionsInteractor(params, ::onSubscribe, ::onFinally, ::onGetLinkedCollections, ::onError)
        interactor = getLinkedCollectionsInteractor
    }

    private fun onGetLinkedCollections(collections: List<Collection>) {
        val names = collections.map { it.name ?: "undefined" }
        _getLinkedCollectionsLiveData.value = EventArgs(names)
    }

    fun updateCollectionDate(collectionId: Long) {
        val params = UpdateCollectionDateInteractor.Params(collectionId)
        updateCollectionDateInteractor(params, ::onSubscribe, ::onFinally, ::onUpdateCollectionDate, ::onError)
        interactor = updateCollectionDateInteractor
    }

    private fun onUpdateCollectionDate(unit: Unit) {
    }
}
