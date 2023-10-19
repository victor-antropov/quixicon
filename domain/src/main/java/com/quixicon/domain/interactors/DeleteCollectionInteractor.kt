package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class DeleteCollectionInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Unit, DeleteCollectionInteractor.Params>(scheduler, postScheduler) {

    data class Params(val collectionId: Long)
}

class DeleteCollectionUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : DeleteCollectionInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Unit> {
        return databaseBoundary.deleteCollection(params.collectionId)
    }
}
