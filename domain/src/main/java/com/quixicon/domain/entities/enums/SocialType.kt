package com.quixicon.domain.entities.enums

import com.google.gson.annotations.JsonAdapter
import com.quixicon.domain.adapters.ValuedEnumAdapter

/**
 * Enum class for card types
 */
@JsonAdapter(ValuedEnumAdapter::class)
enum class SocialType(override val value: String) : ValuedEnum<String> {
    TELEGRAM("telegram"),
    VK("vk"),
    FACEBOOK("facebook"),
    INSTAGRAM("instagram"),
    TWITTER("twitter")
}
