package com.quixicon.domain.entities.enums

import com.google.gson.annotations.JsonAdapter
import com.quixicon.domain.adapters.ValuedEnumAdapter

/**
 * Enum class for collection sorting order
 */
@JsonAdapter(ValuedEnumAdapter::class)
enum class CollectionSortOrder(override val value: Byte) : ValuedEnum<Byte> {
    AZ(0),
    RECENT(1)
}
