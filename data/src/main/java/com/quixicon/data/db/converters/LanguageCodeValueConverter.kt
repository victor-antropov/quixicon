package com.quixicon.data.db.converters

import androidx.room.TypeConverter
import com.quixicon.domain.entities.enums.LanguageCode

object LanguageCodeValueConverter {

    @TypeConverter
    @JvmStatic
    fun typeEnumToValue(type: LanguageCode?): String? {
        return type?.value
    }

    @TypeConverter
    @JvmStatic
    fun typeValueToEnum(value: String?) =
        value?.let {
            var type: LanguageCode? = null
            for (temp in enumValues<LanguageCode>()) {
                if (temp.value == value) {
                    type = temp
                    break
                }
            }
            type
        }
}
