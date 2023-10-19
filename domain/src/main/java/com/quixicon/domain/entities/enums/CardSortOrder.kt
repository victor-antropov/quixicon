package com.quixicon.domain.entities.enums

import com.google.gson.annotations.JsonAdapter
import com.quixicon.domain.adapters.ValuedEnumAdapter

/**
 * Enum class for card sorting order
 */
@JsonAdapter(ValuedEnumAdapter::class)
enum class CardSortOrder(override val value: Byte) : ValuedEnum<Byte> {
    DEFAULT(0),
    AZ(1),
    RECENT(2),
    OLDEST(3),
    TYPE(4),
    UNKNOWN(5),
    KNOWN(6)
}
