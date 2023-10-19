package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

abstract class GetCardsForTestInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<Card>, GetCardsForTestInteractor.Params>(scheduler, postScheduler) {

    data class Params(val id: Long, val shuffle: Boolean, val showAll: Boolean, val usePart: Boolean, val size: Int? = null, val volume: Int? = null)
}

class GetCardsForTestUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetCardsForTestInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<Card>> {
        return Single.fromCallable {
            val cards = databaseBoundary.selectCardsForTest(
                collectionId = params.id,
                showAll = params.showAll,
                limit = if (params.usePart) params.size else null,
                offset = if (params.usePart && params.size != null && params.volume != null) (params.volume - 1) * params.size else null
            ).delay(1000, TimeUnit.MILLISECONDS).blockingGet().toMutableList()

            if (params.shuffle) {
                if (cards.size > 20) {
                    val lim1: Int = cards.size / 3
                    val lim2 = lim1 * 2
                    cards.subList(lim1, cards.size).shuffle()
                    cards.subList(0, lim2).shuffle()
                } else {
                    cards.shuffle()
                }
            }
            cards
        }
    }
}
