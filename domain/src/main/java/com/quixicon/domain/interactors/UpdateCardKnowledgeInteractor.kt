package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class UpdateCardKnowledgeInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Int, UpdateCardKnowledgeInteractor.Params>(scheduler, postScheduler) {

    data class Params(val id: Long, val value: Int)
}

class UpdateCardKnowledgeUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : UpdateCardKnowledgeInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Int> {
        return databaseBoundary.updateCardKnowledge(params.id, params.value)
    }
}
