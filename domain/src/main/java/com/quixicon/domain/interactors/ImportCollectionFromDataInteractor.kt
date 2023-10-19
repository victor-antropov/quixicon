package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.remotes.CollectionData
import com.quixicon.domain.entities.rx.transformers.SimpleSingleTransformer
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Named

abstract class ImportCollectionFromDataInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Long, ImportCollectionFromDataInteractor.Params>(
        scheduler,
        postScheduler
    ) {

    data class Params(
        val data: CollectionData
    )
}

class ImportCollectionFromDataUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    private val longTransformer: SimpleSingleTransformer<Long>,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : ImportCollectionFromDataInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Long> {
        return if (params.data.collectionType == CollectionType.SUBCORE) {
            databaseBoundary.insertCollectionData(params.data)
        } else {
            databaseBoundary.insertCollectionDataSimple(params.data)
        }.compose(longTransformer)
    }
}
