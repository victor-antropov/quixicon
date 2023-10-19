package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.boundaries.ServerBoundary
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.enums.CollectionPriceCategory
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionInfo
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import javax.inject.Inject
import javax.inject.Named

abstract class GetRemoteCollectionsInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<Pair<CollectionInfo, Long>>, GetRemoteCollectionsInteractor.Params>(scheduler, postScheduler) {

    data class Params(
        val filterSubject: LanguageCode?,
        val filterStudent: LanguageCode?,
        val category: CollectionPriceCategory,
        val isMultiLanguages: Boolean
    )
}

class GetRemoteCollectionsUseCase @Inject constructor(
    private val serverBoundary: ServerBoundary,
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetRemoteCollectionsInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<Pair<CollectionInfo, Long>>> {
        val installedCollections = mutableListOf<Collection>()

        // Параметры для запроса и последующей фильтрации
        val reqSubject: LanguageCode?
        val reqStudent: LanguageCode?
        val filterSubject: LanguageCode?
        val filterStudent: LanguageCode?

        if (params.isMultiLanguages) {
            when (params.filterSubject) {
                LanguageCode.EN -> {
                    reqSubject = params.filterSubject
                    reqStudent = null
                    filterSubject = null
                }
                LanguageCode.ES -> {
                    reqSubject = params.filterSubject
                    reqStudent = null
                    filterSubject = null
                }
                LanguageCode.FR -> {
                    reqSubject = params.filterSubject
                    reqStudent = null
                    filterSubject = null
                }
                LanguageCode.SV -> {
                    reqSubject = params.filterSubject
                    reqStudent = null
                    filterSubject = null
                }
                else -> {
                    reqSubject = null
                    reqStudent = null
                    filterSubject = params.filterSubject
                }
            }
            filterStudent = params.filterStudent
        } else {
            reqSubject = params.filterSubject
            reqStudent = params.filterStudent
            filterSubject = null
            filterStudent = null
        }

        return databaseBoundary.selectCollections(CollectionSortOrder.RECENT).flatMap {
            Singles.zip(
                Single.just(it),
                serverBoundary.getCollections(reqSubject, reqStudent, params.category)
            )
        }.map {
            installedCollections.addAll(it.first)
            it.second
        }.flattenAsObservable { it.asIterable() }
            .filter {
                (filterSubject == null || it.codeSubject == filterSubject) && (filterStudent == null || it.codeStudent == filterStudent)
            }
            .map { collection ->
                val result = installedCollections.firstOrNull { it.imei == collection.imei }
                Pair(collection, result?.id ?: 0)
            }
            .toList()
            .map { ArrayList<Pair<CollectionInfo, Long>>(it) }
    }
}
