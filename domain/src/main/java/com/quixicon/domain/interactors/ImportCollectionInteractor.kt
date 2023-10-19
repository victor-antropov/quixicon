package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.AssetsBoundary
import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.boundaries.ServerBoundary
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.rx.transformers.SimpleSingleTransformer
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Named

abstract class ImportCollectionInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Long, ImportCollectionInteractor.Params>(
        scheduler,
        postScheduler
    ) {

    data class Params(
        val subject: LanguageCode,
        val student: LanguageCode,
        val url: String,
        val isLocal: Boolean = true
    )
}

class ImportCollectionUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    private val assetsBoundary: AssetsBoundary,
    private val serverBoundary: ServerBoundary,
    private val longTransformer: SimpleSingleTransformer<Long>,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : ImportCollectionInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Long> {
        return if (params.isLocal) {
            assetsBoundary.readCollection(params.url)
        } else {
            serverBoundary.readCollection(params.subject, params.student, params.url)
        }.flatMap {
            if (it.collectionType == CollectionType.SUBCORE) {
                databaseBoundary.insertCollectionData(it)
            } else {
                databaseBoundary.insertCollectionDataSimple(it)
            }
        }.compose(longTransformer)
    }
}
