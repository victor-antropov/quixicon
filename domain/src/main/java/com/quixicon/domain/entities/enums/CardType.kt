package com.quixicon.domain.entities.enums

import com.google.gson.annotations.JsonAdapter
import com.quixicon.domain.adapters.ValuedEnumAdapter

/**
 * Enum class for card types
 */
@JsonAdapter(ValuedEnumAdapter::class)
enum class CardType(override val value: Byte) : ValuedEnum<Byte> {
    WORD(0),
    PHRASE(1),
    RULE(2)
}
