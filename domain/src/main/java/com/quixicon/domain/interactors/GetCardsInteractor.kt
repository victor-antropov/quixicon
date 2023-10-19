package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

abstract class GetCardsInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<Card>, GetCardsInteractor.Params>(scheduler, postScheduler) {

    data class Params(
        val collectionId: Long = 0,
        val superType: CardType? = null,
        val order: CardSortOrder = CardSortOrder.AZ,
        val subjectFilter: LanguageCode? = null
    )
}

class GetCardsUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetCardsInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<Card>> {
        Timber.e("Select cards with $params")

        return when {
            params.collectionId != 0L -> databaseBoundary.selectCardsByCollectionId(params.collectionId, params.order, params.subjectFilter)
            params.superType != null -> databaseBoundary.selectCardsByType(params.superType, params.order, params.subjectFilter)
            else -> databaseBoundary.selectAllCards(params.order, params.subjectFilter)
        }
    }
}
