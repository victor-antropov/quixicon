package com.quixicon.domain.entities.enums

import com.google.gson.annotations.JsonAdapter
import com.quixicon.domain.adapters.ValuedEnumAdapter

/**
 * Enum class for collection types
 */
@JsonAdapter(ValuedEnumAdapter::class)
enum class CollectionType(override val value: Byte) : ValuedEnum<Byte> {
    DEFAULT(0),
    CORE(1),
    SUBCORE(2),
    USER(3),
    EXAMPLE(4)
}
