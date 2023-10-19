package com.quixicon.presentation.activities.social.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import com.quixicon.R
import com.quixicon.core.lifecycle.PendingLiveData
import com.quixicon.core.system.EventArgs
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.SocialNetwork
import com.quixicon.domain.interactors.GetRemoteSocialNetworksInteractor
import com.quixicon.domain.mappers.ListMapperImpl
import com.quixicon.presentation.activities.social.mappers.SocialNetworkModelMapper
import com.quixicon.presentation.activities.social.models.SocialNetworkModel
import com.quixicon.presentation.viewmodels.BaseViewModel
import javax.inject.Inject

class SocialViewModel @Inject constructor(
    private val getRemoteSocialNetworksInteractor: GetRemoteSocialNetworksInteractor,
    mapper: SocialNetworkModelMapper,
    val app: Application
) : BaseViewModel(app) {

    override val interactors = listOf(
        getRemoteSocialNetworksInteractor
    )

    private val listMapper = ListMapperImpl(mapper)

    private val _getSocialNetworksLiveData = PendingLiveData<EventArgs<List<SocialNetworkModel>>>()
    val getSocialNetworksLiveData: LiveData<EventArgs<List<SocialNetworkModel>>> = _getSocialNetworksLiveData

    fun invokeLatestRequest() {
        interactor?.invokeLatestRequest()
    }

    fun getRemoteSocialNetworks() {
        val isMultiLanguage = !app.baseContext.resources.getBoolean(R.bool.use_only_language)
        val params = GetRemoteSocialNetworksInteractor.Params(subject = LanguageCode.EN, student = LanguageCode.RU, isMultiLanguage)
        getRemoteSocialNetworksInteractor(params, ::onSubscribe, ::onFinally, ::onGetSocialNetworks, ::onError)
        interactor = getRemoteSocialNetworksInteractor
    }

    private fun onGetSocialNetworks(items: List<SocialNetwork>) {
        val collectionModels = listMapper.mapToOutput(items.filter { it.enable })
        _getSocialNetworksLiveData.value = EventArgs(collectionModels)
    }
}
