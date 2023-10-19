package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

abstract class UpdateCardInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<Unit, UpdateCardInteractor.Params>(scheduler, postScheduler) {

    data class Params(val card: Card, val collectionId: Long? = null)
}

class UpdateCardUseCase @Inject constructor(
    private val databaseBoundary: DatabaseBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : UpdateCardInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<Unit> {
        return if (params.card.id > 0) {
            Timber.e("Update Card: ${params.card}")
            databaseBoundary.updateCard(params.card)
        } else {
            Timber.e("Insert Card: ${params.card}")
            if (params.collectionId!! > 0) {
                databaseBoundary.getCollection(params.collectionId).flatMap {
                    val card = params.card.apply { subject = it.codeSubject }
                    Timber.e("InsertCard: ${card.subject}")
                    databaseBoundary.insertCard(params.card, params.collectionId)
                }
            } else {
                throw IllegalArgumentException("collectionId must be possitive")
            }
        }
    }
}
