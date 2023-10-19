package com.quixicon.domain.boundaries

import com.quixicon.domain.entities.enums.CollectionPriceCategory
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionData
import com.quixicon.domain.entities.remotes.CollectionInfo
import com.quixicon.domain.entities.remotes.CollectionMap
import com.quixicon.domain.entities.remotes.SocialNetwork
import io.reactivex.Single

interface ServerBoundary {

    fun getCollections(subject: LanguageCode?, student: LanguageCode?, category: CollectionPriceCategory): Single<List<CollectionInfo>>

    fun readCollection(subject: LanguageCode, student: LanguageCode, url: String): Single<CollectionData>

    fun getCollectionsMap(subject: LanguageCode, student: LanguageCode, isMultiLanguages: Boolean): Single<List<CollectionMap>>

    fun getSocialNetworks(subject: LanguageCode, student: LanguageCode, isMultiLanguages: Boolean): Single<List<SocialNetwork>>
}
