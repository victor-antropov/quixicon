package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class GetCollectionInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Collection, GetCollectionInteractor.Params>(scheduler, postScheduler) {

    data class Params(val id: Long)
}

class GetCollectionUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetCollectionInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Collection> {
        return databaseBoundary.getCollection(params.id)
    }
}
