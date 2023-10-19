package com.quixicon.data.network.api

import com.quixicon.data.network.entities.RemoteCollectionData
import com.quixicon.data.network.entities.RemoteCollectionInfo
import com.quixicon.data.network.entities.RemoteCollectionMap
import com.quixicon.data.network.entities.RemoteSocialNetwork
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ServerApiService {

    @GET("/v2/data/{link}")
    fun getCollection(@Path("link", encoded = true) link: String): Single<Response<RemoteCollectionData>>

    @GET("/v2/catalog/{subject}/{student}/collections_free.json")
    fun getFreeCollections(@Path("subject") subject: String, @Path("student") student: String): Single<Response<List<RemoteCollectionInfo>>>

    @GET("/v2/catalog/{subject}/{student}/collections_premium.json")
    fun getPremiumCollections(@Path("subject") subject: String, @Path("student") student: String): Single<Response<List<RemoteCollectionInfo>>>

    @GET("/v2/catalog/{subject}/{student}/collections_map.json")
    fun getCollectionsMap(@Path("subject") subject: String, @Path("student") student: String): Single<Response<List<RemoteCollectionMap>>>

    @GET("/v2/catalog/{subject}/{student}/social.json")
    fun getSocialNetworks(@Path("subject") subject: String, @Path("student") student: String): Single<Response<List<RemoteSocialNetwork>>>
}
