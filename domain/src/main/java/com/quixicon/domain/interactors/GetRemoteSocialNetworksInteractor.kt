package com.quixicon.domain.interactors

import com.quixicon.domain.boundaries.ServerBoundary
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.SocialNetwork
import com.quixicon.domain.interactors.base.BaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

abstract class GetRemoteSocialNetworksInteractor(scheduler: Scheduler, postScheduler: Scheduler) :
    BaseInteractor<List<SocialNetwork>, GetRemoteSocialNetworksInteractor.Params>(scheduler, postScheduler) {

    data class Params(
        val subject: LanguageCode,
        val student: LanguageCode,
        val isMultiLanguages: Boolean
    )
}

class GetRemoteSocialNetworksUseCase @Inject constructor(
    private val serverBoundary: ServerBoundary,
    @Named("IoScheduler") scheduler: Scheduler,
    @Named("MainScheduler") postScheduler: Scheduler
) : GetRemoteSocialNetworksInteractor(scheduler, postScheduler) {

    override fun createSingle(params: Params): Single<List<SocialNetwork>> {
        return serverBoundary.getSocialNetworks(params.subject, params.student, params.isMultiLanguages)
    }
}
