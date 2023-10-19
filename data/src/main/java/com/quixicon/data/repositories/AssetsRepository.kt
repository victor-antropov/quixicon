package com.quixicon.data.repositories

import android.app.Application
import com.google.gson.Gson
import com.quixicon.data.network.entities.RemoteCollectionData
import com.quixicon.data.network.mappers.RemoteCollectionMapper
import com.quixicon.data.repositories.base.BaseRepository
import com.quixicon.domain.boundaries.AssetsBoundary
import com.quixicon.domain.entities.remotes.CollectionData
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class AssetsRepository @Inject constructor(
    private val app: Application,
    private val mapper: RemoteCollectionMapper
) : BaseRepository(), AssetsBoundary {

    override fun readCollection(url: String): Single<CollectionData> {
        return Single.fromCallable {
            try {
                val input = app.assets.open(url)
                val size = input.available()
                val buffer = ByteArray(size)
                input.read(buffer)
                input.close()
                val json = String(buffer, StandardCharsets.UTF_8)
                val col: RemoteCollectionData = Gson().fromJson(json, RemoteCollectionData::class.java)
                mapper.mapToInput(col)
            }
            catch (e: Exception) {
                e.printStackTrace()
                Timber.e("Exception: $e")
                null
            }
        }
    }

    override fun readCollections(urls: List<String>): Single<List<CollectionData>> {
        return Single.just(urls).flattenAsObservable { it.asIterable() }
            .map {
                val input = app.assets.open(it)
                val size = input.available()
                val buffer = ByteArray(size)
                input.read(buffer)
                input.close()
                val json = String(buffer, StandardCharsets.UTF_8)
                val col: RemoteCollectionData = Gson().fromJson(json, RemoteCollectionData::class.java)
                mapper.mapToInput(col)
            }.toList()
    }

    override fun readCollectionsObservable(urls: List<String>): Observable<CollectionData> {
        return Single.just(urls).flattenAsObservable { it.asIterable() }
            .map {
                val input = app.assets.open(it)
                val size = input.available()
                val buffer = ByteArray(size)
                input.read(buffer)
                input.close()
                val json = String(buffer, StandardCharsets.UTF_8)
                val col: RemoteCollectionData = Gson().fromJson(json, RemoteCollectionData::class.java)
                mapper.mapToInput(col)
            }
    }
}
