package com.quixicon.domain.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type

/**
 * Adapter for deserialization instances of [OffsetDateTime] types from string type.
 */
class OffsetDateTimeAdapter : JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): OffsetDateTime {
        val dateTimeString = json.asJsonPrimitive.asString
        val localDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
        return OffsetDateTime.of(localDateTime, ZoneOffset.UTC)
    }

    override fun serialize(
        src: OffsetDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val str = src?.toLocalDateTime().toString()
        return JsonPrimitive(str)
    }
}
