package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class CopyCardInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Long, CopyCardInteractor.Params>(scheduler, postScheduler) {

    data class Params(val cardId: Long, val collectionId: Long)
}

class CopyCardUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : CopyCardInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Long> {
        return databaseBoundary.copyCard(params.cardId, params.collectionId)
    }
}
