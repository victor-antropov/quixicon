package com.quixicon.data.network.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.quixicon.domain.entities.enums.CardType
import java.io.Serializable

data class RemoteCardData(
    @Expose
    @SerializedName("name")
    val name: String,
    @Expose
    @SerializedName("translation")
    val translation: String? = null,

    @Expose
    @SerializedName("description")
    val description: String? = null,
    @Expose
    @SerializedName("transcription")
    val transcription: String? = null,

    @Expose
    @SerializedName("card_type")
    val cardType: CardType? = null,
    @Expose
    @SerializedName("knowledge")
    val knowledge: Int? = null,
    @Expose
    @SerializedName("uid")
    val uid: Long? = null
) : Serializable
