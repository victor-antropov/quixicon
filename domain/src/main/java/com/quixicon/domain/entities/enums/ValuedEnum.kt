package com.quixicon.domain.entities.enums

import java.io.Serializable

/**
 * Interface for implements in enum classes, which must be store [value] with [T] type.
 *
 * @param T Type for stored value.
 */
interface ValuedEnum<T : Any> : Serializable {

    val value: T
}
