package com.quixicon.domain.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.quixicon.domain.entities.enums.ValuedEnum
import java.lang.reflect.Type

/**
 * Adapter for serialization / deserialization instances of [ValuedEnum] types from / to
 * primitive types.
 */
class ValuedEnumAdapter<T : ValuedEnum<Any>> : JsonSerializer<T>, JsonDeserializer<T> {

    private var map: Map<Any, T>? = null
    private var value: Any? = null

    override fun serialize(
        src: T,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return serialize(src.value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): T {
        return (
            map ?: (typeOfT as Class<T>).enumConstants!!.associateBy { valuedEnum ->
                value = valuedEnum.value
                valuedEnum.value
            }.also { map = it }
            ).getValue(deserialize(json))
    }

    private fun serialize(value: Any): JsonPrimitive =
        when (value) {
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            else -> throw IllegalArgumentException("Not yet implemented")
        }

    private fun deserialize(json: JsonElement): Any {
        if (json.isJsonPrimitive) {
            with(json as JsonPrimitive) {
                return when {
                    isBoolean -> asBoolean
                    isNumber -> convertToNumber(json)
                    isString -> asString
                    else -> throw IllegalArgumentException("Not yet implemented")
                }
            }
        }
        throw IllegalArgumentException("JsonElement is not JsonPrimitive")
    }

    private fun convertToNumber(json: JsonPrimitive): Number =
        when (value) {
            is Int -> json.asInt
            is Long -> json.asLong
            is Short -> json.asShort
            is Byte -> json.asByte
            else -> throw IllegalArgumentException("Not yet implemented")
        }
}
