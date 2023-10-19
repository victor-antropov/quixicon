package com.quixicon.data.db.converters

import androidx.room.TypeConverter
import com.quixicon.domain.entities.enums.CardType

object CardTypeValueConverter {

    @TypeConverter
    @JvmStatic
    fun typeEnumToValue(type: CardType?) = type?.value

    @TypeConverter
    @JvmStatic
    fun typeValueToEnum(value: Byte?) =
        value?.let {
            var type: CardType? = null
            for (temp in enumValues<CardType>()) {
                if (temp.value == value) {
                    type = temp
                    break
                }
            }
            type
        }
}
