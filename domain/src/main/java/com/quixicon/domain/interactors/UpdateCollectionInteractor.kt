package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class UpdateCollectionInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Unit, UpdateCollectionInteractor.Params>(scheduler, postScheduler) {

    data class Params(val collection: Collection)
}

class UpdateCollectionUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : UpdateCollectionInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Unit> {
        return if (params.collection.id > 0) {
            databaseBoundary.updateCollection(params.collection.id, params.collection.name, params.collection.description, params.collection.codeSubject)
        } else {
            databaseBoundary.insertCollection(params.collection.name, params.collection.description, params.collection.codeSubject)
        }
    }
}
