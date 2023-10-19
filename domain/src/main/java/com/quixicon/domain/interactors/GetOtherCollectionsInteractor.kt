package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class GetOtherCollectionsInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<Collection>, GetOtherCollectionsInteractor.Params>(scheduler, postScheduler) {

    data class Params(val id: Long, val subjectFilter: LanguageCode? = null)
}

class GetOtherCollectionsUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetOtherCollectionsInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<Collection>> {
        return databaseBoundary.selectOtherCollections(params.id, params.subjectFilter)
    }
}
