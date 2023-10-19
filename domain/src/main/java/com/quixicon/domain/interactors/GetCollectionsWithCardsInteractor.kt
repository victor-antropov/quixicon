package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class GetCollectionsWithCardsInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<Pair<Collection, Int>>, GetCollectionsWithCardsInteractor.Params>(scheduler, postScheduler) {

    data class Params(val subjectFilter: LanguageCode? = null)
}

class GetCollectionsWithCardsUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetCollectionsWithCardsInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<Pair<Collection, Int>>> {
        return databaseBoundary.selectCollectionsWithCards(params.subjectFilter)
    }
}
