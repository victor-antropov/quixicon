package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class UpdateCollectionDateInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Unit, UpdateCollectionDateInteractor.Params>(scheduler, postScheduler) {

    data class Params(val collectionId: Long)
}

class UpdateCollectionDateUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : UpdateCollectionDateInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Unit> {
        return if (params.collectionId > 0) {
            databaseBoundary.updateCollectionDate(params.collectionId)
        } else {
            Single.just(Unit)
        }
    }
}
