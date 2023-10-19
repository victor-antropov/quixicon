package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.presentation.CollectionItem
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class GetCollectionsInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<CollectionItem>, GetCollectionsInteractor.Params>(scheduler, postScheduler) {

    data class Params(
        val order: CollectionSortOrder = CollectionSortOrder.RECENT,
        val subjectFilter: LanguageCode? = null,
        val studentFilter: LanguageCode? = null
    )
}

class GetCollectionsUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetCollectionsInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<CollectionItem>> {
        return Single.fromCallable {
            val output = mutableListOf<CollectionItem>()

            val collectionsPlusInfo = databaseBoundary.selectCollectionsWithSizeInfo(params.order, params.subjectFilter, params.studentFilter).blockingGet()
            val superSizes = databaseBoundary.selectSuperTypeSizes(params.subjectFilter, params.studentFilter).blockingGet()

            superSizes[CardType.WORD]?.let {
                output.add(CollectionItem(qntWords = it, superType = CardType.WORD))
            }
            superSizes[CardType.PHRASE]?.let {
                output.add(CollectionItem(qntPhrases = it, superType = CardType.PHRASE))
            }
            superSizes[CardType.RULE]?.let {
                output.add(CollectionItem(qntRules = it, superType = CardType.RULE))
            }
            output.addAll(
                collectionsPlusInfo.map {
                    CollectionItem(it.first).apply {
                        qntWords = it.second[CardType.WORD] ?: 0
                        qntPhrases = it.second[CardType.PHRASE] ?: 0
                        qntRules = it.second[CardType.RULE] ?: 0
                    }
                }
            )

            output
        }
    }
}
