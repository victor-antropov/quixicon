package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class GetLinkedCollectionsInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<Collection>, GetLinkedCollectionsInteractor.Params>(scheduler, postScheduler) {

    data class Params(val cardId: Long)
}

class GetLinkedCollectionsUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetLinkedCollectionsInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<Collection>> {
        return databaseBoundary.selectLinkedCollections(params.cardId)
    }
}
