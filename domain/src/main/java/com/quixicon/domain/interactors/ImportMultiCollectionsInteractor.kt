package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.AssetsBoundary
import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.rx.transformers.SimpleSingleTransformer
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class ImportMultiCollectionsInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<Long>, ImportMultiCollectionsInteractor.Params>(
        scheduler,
        postScheduler
    ) {

    data class Params(
        val urls: List<String>,
        val subjectLanguageCode: LanguageCode?,
        val studentLanguageCode: LanguageCode?
    )
}

class ImportMultiCollectionsUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    private val assetsBoundary: AssetsBoundary,
    private val longTransformer: SimpleSingleTransformer<List<Long>>,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : ImportMultiCollectionsInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<Long>> {
        fun compareStudent(value: String?): Boolean {
            return if (params.studentLanguageCode == null) {
                true
            } else {
                params.studentLanguageCode.value == value
            }
        }

        fun compareSubject(value: String?): Boolean {
            return if (params.subjectLanguageCode == null) {
                true
            } else {
                params.subjectLanguageCode.value == value
            }
        }

        return assetsBoundary.readCollectionsObservable(params.urls).filter {
            compareStudent(it.codeStudent) and compareSubject(it.codeSubject)
        }.map {
            if (it.collectionType == CollectionType.SUBCORE) {
                databaseBoundary.insertCollectionData(it)
            } else {
                databaseBoundary.insertCollectionDataSimple(it)
            }
        }.map {
            it.blockingGet()
        }.toList().compose(longTransformer)
    }
}
