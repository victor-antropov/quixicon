package com.quixicon.presentation.activities.test.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.interactors.CopyCardInteractor
import com.quixicon.domain.interactors.GetCardsForTestInteractor
import com.quixicon.domain.interactors.GetCollectionsWithCardsInteractor
import com.quixicon.domain.interactors.GetOtherCollectionsInteractor
import com.quixicon.domain.interactors.UpdateCardKnowledgeInteractor
import com.quixicon.domain.interactors.UpdateCollectionDateInteractor
import com.quixicon.domain.mappers.ListMapperImpl
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.mappers.CardActionMapper
import com.quixicon.presentation.activities.cards.mappers.CardModelMapper
import com.quixicon.presentation.activities.cards.models.CardModel
import com.quixicon.presentation.activities.test.mappers.TestCollectionModelMapper
import com.quixicon.presentation.activities.test.models.TestCollectionModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class TestViewModel @Inject constructor(
    private val getCardsForTestInteractor: GetCardsForTestInteractor,
    private val updateCardKnowledgeInteractor: UpdateCardKnowledgeInteractor,
    private val getCollectionsWithCardsInteractor: GetCollectionsWithCardsInteractor,
    private val getOtherCollectionsInteractor: GetOtherCollectionsInteractor,
    private val copyCardInteractor: CopyCardInteractor,
    private val preferencesBoundary: PreferencesBoundary,
    private val updateCollectionDateInteractor: UpdateCollectionDateInteractor,
    cardsMapper: CardModelMapper,
    testCollectionMapper: TestCollectionModelMapper,
    cardActionMapper: CardActionMapper,
    app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        getCardsForTestInteractor,
        updateCardKnowledgeInteractor,
        getCollectionsWithCardsInteractor,
        getOtherCollectionsInteractor,
        copyCardInteractor,
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
    private val collectionListMapper = ListMapperImpl(testCollectionMapper)
    private val otherCollectionsListMapper = ListMapperImpl(cardActionMapper)

    private val _getCardsLiveData = PendingLiveData<EventArgs<List<CardModel>>>()
    val getCardsLiveData: LiveData<EventArgs<List<CardModel>>> = _getCardsLiveData

    private val _getCollectionsLiveData = PendingLiveData<EventArgs<List<TestCollectionModel>>>()
    val getCollectionsLiveData: LiveData<EventArgs<List<TestCollectionModel>>> = _getCollectionsLiveData

    private val _getOtherCollectionsLiveData = PendingLiveData<EventArgs<List<CardAction>>>()
    val getOtherCollectionsLiveData: LiveData<EventArgs<List<CardAction>>> = _getOtherCollectionsLiveData

    private val _getCardCopiedLiveData = PendingLiveData<EventArgs<Unit>>()
    val getCardCopiedLiveData: LiveData<EventArgs<Unit>> = _getCardCopiedLiveData

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun getCardsForTest(id: Long?, shuffle: Boolean, showAll: Boolean, usePart: Boolean, size: Int? = null, volume: Int? = null) {
        if (id != null) {
            val params =
                GetCardsForTestInteractor.Params(id, shuffle, showAll, usePart, size, volume)
            Timber.e("Get test with params $params")
            getCardsForTestInteractor(params, ::onSubscribe, ::onFinally, ::onGetCards, ::onError)
            interactor = getCardsForTestInteractor
        }
    }

    private fun onGetCards(cards: List<Card>) {
        val cardModels = cardsListMapper.mapToOutput(cards)
        _getCardsLiveData.value = EventArgs(cardModels)
    }

    fun getCollections() {
        val filter = if (config.useFilter) config.languageSubject else null
        val params = GetCollectionsWithCardsInteractor.Params(filter)
        getCollectionsWithCardsInteractor(params, ::onSubscribe, ::onFinally, ::onGetCollections, ::onError)
        interactor = getCollectionsWithCardsInteractor
    }

    private fun onGetCollections(collections: List<Pair<Collection, Int>>) {
        val testCollectionModels = collectionListMapper.mapToOutput(collections)
        _getCollectionsLiveData.value = EventArgs(testCollectionModels)
    }

    fun getOtherCollections(id: Long) {
        val filter = if (config.useFilter) config.languageSubject else null
        val params = GetOtherCollectionsInteractor.Params(id, filter)
        getOtherCollectionsInteractor(params, ::onSubscribe, ::onFinally, ::onGetOtherCollections, ::onError)
        interactor = getOtherCollectionsInteractor
    }

    private fun onGetOtherCollections(collections: List<Collection>) {
        val collectionModels = otherCollectionsListMapper.mapToOutput(collections)
        _getOtherCollectionsLiveData.value = EventArgs(collectionModels)
    }

    fun updateCardKnowledge(id: Long, value: Int) {
        val params = UpdateCardKnowledgeInteractor.Params(id, value)
        updateCardKnowledgeInteractor(params, ::onSubscribe, ::onFinally, ::onUpdateCard, ::onError)
        interactor = updateCardKnowledgeInteractor
    }

    private fun onUpdateCard(result: Int) {}

    fun copyCard(cardId: Long, collectionId: Long) {
        val params = CopyCardInteractor.Params(cardId, collectionId)
        copyCardInteractor(params, ::onSubscribe, ::onFinally, ::onCopyCard, ::onError)
        interactor = copyCardInteractor
    }

    private fun onCopyCard(result: Long) {
        Timber.e("Card was copied: $result")
        _getCardCopiedLiveData.value = EventArgs(Unit)
    }

    fun updateCollectionDate(collectionId: Long) {
        val params = UpdateCollectionDateInteractor.Params(collectionId)
        updateCollectionDateInteractor(params, ::onSubscribe, ::onFinally, ::onUpdateCollectionDate, ::onError)
        interactor = updateCollectionDateInteractor
    }

    private fun onUpdateCollectionDate(unit: Unit) {}
}
