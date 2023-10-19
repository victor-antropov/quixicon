package com.quixicon.data.network.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.quixicon.domain.entities.enums.SocialType
import java.io.Serializable

data class RemoteSocialNetwork(
    @Expose
    @SerializedName("type")
    val type: SocialType,
    @Expose
    @SerializedName("url")
    val url: String,
    @Expose
    @SerializedName("enable")
    val enable: Boolean?
) : Serializable
