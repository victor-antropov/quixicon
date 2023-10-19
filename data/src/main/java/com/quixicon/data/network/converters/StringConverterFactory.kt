package com.quixicon.data.network.converters

import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.*

class StringConverterFactory private constructor() : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        if (type == Date::class.java) {
            annotations.forEach {
                if (it is DateToLongConverter.DateToLong) {
                    return DateToLongConverter(it)
                }
            }
        }
        return super.stringConverter(type, annotations, retrofit)
    }

    companion object {
        @JvmStatic
        fun create() = StringConverterFactory()
    }
}
