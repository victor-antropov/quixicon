package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class DeleteCardInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Unit, DeleteCardInteractor.Params>(scheduler, postScheduler) {

    data class Params(val cardId: Long, val collectionId: Long?)
}

class DeleteCardUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : DeleteCardInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Unit> {
        return databaseBoundary.deleteCard(params.cardId, params.collectionId)
    }
}
