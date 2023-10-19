package com.quixicon.data.network.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.quixicon.domain.entities.enums.LanguageCode
import java.io.Serializable

data class RemoteCollectionInfo(
    @Expose
    @SerializedName("name")
    val name: String,
    @Expose
    @SerializedName("description")
    val description: String? = null,
    @Expose
    @SerializedName("code_subject")
    val codeSubject: LanguageCode? = null,
    @Expose
    @SerializedName("code_student")
    val codeStudent: LanguageCode? = null,
    @Expose
    @SerializedName("imei")
    val imei: String? = null,
    @Expose
    @SerializedName("link")
    val link: String? = null,
    @Expose
    @SerializedName("cnt_words")
    val cntWords: Int = 0,
    @Expose
    @SerializedName("cnt_phrases")
    val cntPhrases: Int = 0,
    @Expose
    @SerializedName("cnt_rules")
    val cntRules: Int = 0
) : Serializable
