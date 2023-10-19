package com.quixicon.data.network.converters

import retrofit2.Converter
import java.util.*

class DateToLongConverter(private val dateToLong: DateToLong) : Converter<Date, String> {

    override fun convert(value: Date): String {
        return when (dateToLong.unit) {
            DateToLong.Unit.SEC -> (value.time / 1000).toString()
            DateToLong.Unit.MILLIS -> value.time.toString()
        }
    }

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class DateToLong(val unit: Unit = Unit.SEC) {
        enum class Unit {
            SEC,
            MILLIS
        }
    }
}
