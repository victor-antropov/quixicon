package com.quixicon.domain.entities.remotes

import com.quixicon.domain.entities.enums.LanguageCode
import java.io.Serializable

data class CollectionInfo(
    val name: String,
    val description: String? = null,
    val codeSubject: LanguageCode? = null,
    val codeStudent: LanguageCode? = null,
    val imei: String? = null,
    val link: String? = null,
    val cntWords: Int = 0,
    val cntPhrases: Int = 0,
    val cntRules: Int = 0
) : Serializable
