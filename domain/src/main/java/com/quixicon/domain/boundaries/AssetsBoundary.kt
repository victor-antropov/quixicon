package com.quixicon.domain.boundaries

import com.quixicon.domain.entities.remotes.CollectionData
import io.reactivex.Observable
import io.reactivex.Single

interface AssetsBoundary {

    fun readCollection(url: String): Single<CollectionData>

    fun readCollections(urls: List<String>): Single<List<CollectionData>>

    fun readCollectionsObservable(urls: List<String>): Observable<CollectionData>
}
