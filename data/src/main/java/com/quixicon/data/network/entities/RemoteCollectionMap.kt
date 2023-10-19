package com.quixicon.data.network.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RemoteCollectionMap(
    @Expose
    @SerializedName("id")
    val id: String,
    @Expose
    @SerializedName("url")
    val url: String
) : Serializable
