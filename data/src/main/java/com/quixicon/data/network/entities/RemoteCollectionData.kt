package com.quixicon.data.network.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.quixicon.domain.entities.enums.CollectionType
import java.io.Serializable

data class RemoteCollectionData(
    @Expose
    @SerializedName("name")
    val name: String,
    @Expose
    @SerializedName("description")
    val description: String? = null,
    @Expose
    @SerializedName("code_subject")
    val codeSubject: String? = null,
    @Expose
    @SerializedName("code_student")
    val codeStudent: String? = null,
    @Expose
    @SerializedName("imei")
    val imei: String? = null,
    @Expose
    @SerializedName("cards")
    var cards: List<RemoteCardData>? = null,
    @Expose
    @SerializedName("collection_type")
    val collectionType: CollectionType? = CollectionType.DEFAULT
) : Serializable
