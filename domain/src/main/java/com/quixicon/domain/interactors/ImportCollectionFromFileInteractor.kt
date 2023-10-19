package com.quixicon.domain.interactors

import android.net.Uri
import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.boundaries.FilesBoundary
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionData
import com.quixicon.domain.entities.rx.transformers.SimpleSingleTransformer
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named

abstract class ImportCollectionFromFileInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Long, ImportCollectionFromFileInteractor.Params>(
        scheduler,
        postScheduler
    ) {
    data class Params(
        val uri: Uri,
        val nameTemplate: String,
        val subjectFilter: LanguageCode? = null
    )
}

class ImportCollectionFromFileUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    private val filesBoundary: FilesBoundary,
    private val longTransformer: SimpleSingleTransformer<Long>,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : ImportCollectionFromFileInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Long> {
        return filesBoundary.readCollection(params.uri)
            .map {
                CollectionData(
                    name = it.name,
                    description = it.description,
                    codeSubject = params.subjectFilter?.value,
                    codeStudent = it.codeStudent,
                    cards = it.cards,
                    collectionType = it.collectionType
                )
            }.flatMap {
                databaseBoundary.insertCollectionData(it)
            }.map {
                val name = String.format(params.nameTemplate, it)
                Timber.e("New name for collection $it: $name ")
                databaseBoundary.updateCollection(
                    it,
                    String.format(params.nameTemplate, it),
                    "",
                    params.subjectFilter
                ).blockingGet()
                it
            }.compose(longTransformer)
    }
}
