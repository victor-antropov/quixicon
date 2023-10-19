package com.quixicon.domain.entities.remotes

import com.quixicon.domain.entities.enums.SocialType
import java.io.Serializable

data class SocialNetwork(
    val type: SocialType,
    val url: String,
    val enable: Boolean = true
) : Serializable
