package com.quixicon.domain.entities.db

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CardExtraData(
    @Expose
    @SerializedName("description")
    val description: String? = null
) : Serializable
