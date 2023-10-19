package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class GetCardInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Card, GetCardInteractor.Params>(scheduler, postScheduler) {

    data class Params(val id: Long)
}

class GetCardUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetCardInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Card> {
        return databaseBoundary.getCard(params.id)
    }
}
