package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.boundaries.ServerBoundary
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionData
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import javax.inject.Inject
import javax.inject.Named

abstract class GetRemoteCollectionMapInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Pair<CollectionData, Long>, GetRemoteCollectionMapInteractor.Params>(scheduler, postScheduler) {

    data class Params(
        val subject: LanguageCode,
        val student: LanguageCode,
        val id: String,
        val isMultiLanguages: Boolean
    )
}

class GetRemoteCollectionMapUseCase @Inject constructor(
    private val serverBoundary: ServerBoundary,
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetRemoteCollectionMapInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Pair<CollectionData, Long>> {
        val installed = databaseBoundary.selectCollections(CollectionSortOrder.RECENT).flattenAsObservable { it.asIterable() }

        return serverBoundary.getCollectionsMap(params.subject, params.student, params.isMultiLanguages).flattenAsObservable { it.asIterable() }
            .filter {
                it.id == params.id
            }.firstOrError().flatMap {
                serverBoundary.readCollection(params.subject, params.student, it.url)
            }.flatMap {
                Singles.zip(
                    Single.just(it),
                    installed.filter { ic ->
                        ic.imei == it.imei
                    }.map { item -> item.id }.first(0L)
                )
            }
    }
}
