package com.quixicon.data.db.converters

import androidx.room.TypeConverter
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

object DateTimeStringConverter {

    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun stringToDateTime(value: String?) =
        value?.let { formatter.parse(value, OffsetDateTime::from) }

    @TypeConverter
    @JvmStatic
    fun dateTimeToString(dateTime: OffsetDateTime?) = dateTime?.format(formatter)
}
