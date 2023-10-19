package com.quixicon.data.repositories

import com.quixicon.data.network.INetworkUtils
import com.quixicon.data.network.api.ServerApiService
import com.quixicon.data.network.mappers.RemoteCollectionInfoMapper
import com.quixicon.data.network.mappers.RemoteCollectionMapMapper
import com.quixicon.data.network.mappers.RemoteCollectionMapper
import com.quixicon.data.network.mappers.RemoteSocialNetworkMapper
import com.quixicon.data.repositories.base.BaseRepository
import com.quixicon.domain.boundaries.ServerBoundary
import com.quixicon.domain.entities.enums.CollectionPriceCategory
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionData
import com.quixicon.domain.entities.remotes.CollectionInfo
import com.quixicon.domain.entities.remotes.CollectionMap
import com.quixicon.domain.entities.remotes.SocialNetwork
import com.quixicon.domain.exceptions.Failure
import com.quixicon.domain.mappers.ListMapperImpl
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class ServerRepository @Inject constructor(
    private val networkUtils: INetworkUtils,
    private val apiService: ServerApiService,
    collectionInfoMapper: RemoteCollectionInfoMapper,
    collectionMapMapper: RemoteCollectionMapMapper,
    socialNetworkMapper: RemoteSocialNetworkMapper,
    private val collectionDataMapper: RemoteCollectionMapper
) : BaseRepository(), ServerBoundary {

    private val listInfoMapper = ListMapperImpl(collectionInfoMapper)

    private val listMapMapper = ListMapperImpl(collectionMapMapper)

    private val listSocialMapper = ListMapperImpl(socialNetworkMapper)

    private val PATH_ALL = "all"

    override fun getCollections(
        subject: LanguageCode?,
        student: LanguageCode?,
        category: CollectionPriceCategory
    ): Single<List<CollectionInfo>> {
        return when (networkUtils.isNetworkAvailable()) {
            true -> request(
                if (category == CollectionPriceCategory.FREE) {
                    apiService.getFreeCollections(subject?.value ?: PATH_ALL, student?.value ?: PATH_ALL)
                } else {
                    apiService.getPremiumCollections(subject?.value ?: PATH_ALL, student?.value ?: PATH_ALL)
                },
                listInfoMapper::mapToInput
            )
            false -> error(Failure.ConnectionError)
        }
    }

    override fun readCollection(
        subject: LanguageCode,
        student: LanguageCode,
        url: String
    ): Single<CollectionData> {
        Timber.e("Read collection from URL: $url")
        return when (networkUtils.isNetworkAvailable()) {
            true -> request(
                apiService.getCollection(url),
                collectionDataMapper::mapToInput
            )
            false -> error(Failure.ConnectionError)
        }
    }

    override fun getCollectionsMap(
        subject: LanguageCode,
        student: LanguageCode,
        isMultiLanguages: Boolean
    ): Single<List<CollectionMap>> {
        return when (networkUtils.isNetworkAvailable()) {
            true -> request(
                if (isMultiLanguages) {
                    apiService.getCollectionsMap(PATH_ALL, PATH_ALL)
                } else {
                    apiService.getCollectionsMap(subject.value, PATH_ALL)
                },
                listMapMapper::mapToInput
            )
            false -> error(Failure.ConnectionError)
        }
    }

    override fun getSocialNetworks(
        subject: LanguageCode,
        student: LanguageCode,
        isMultiLanguages: Boolean
    ): Single<List<SocialNetwork>> {
        return when (networkUtils.isNetworkAvailable()) {
            true -> request(
                if (isMultiLanguages) {
                    apiService.getSocialNetworks(PATH_ALL, PATH_ALL)
                } else {
                    apiService.getSocialNetworks(subject.value, student.value)
                },
                listSocialMapper::mapToInput
            )
            false -> error(Failure.ConnectionError)
        }
    }
}
