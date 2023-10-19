package com.quixicon.data.db.converters

import androidx.room.TypeConverter
import com.quixicon.domain.entities.enums.CollectionType

object CollectionTypeValueConverter {

    @TypeConverter
    @JvmStatic
    fun typeEnumToValue(type: CollectionType?) = type?.value

    @TypeConverter
    @JvmStatic
    fun typeValueToEnum(value: Byte?) =
        value?.let {
            var type: CollectionType? = null
            for (temp in enumValues<CollectionType>()) {
                if (temp.value == value) {
                    type = temp
                    break
                }
            }
            type
        }
}
