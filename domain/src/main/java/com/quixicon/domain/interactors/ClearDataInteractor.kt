package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class ClearDataInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Unit, Unit>(scheduler, postScheduler)

class ClearDataUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : ClearDataInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Unit): Single<Unit> {
        return databaseBoundary.clearDb()
    }
}
