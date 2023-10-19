package com.quixicon.data.db.entities

import androidx.room.TypeConverters
import com.quixicon.data.db.converters.CardTypeValueConverter
import com.quixicon.domain.entities.enums.CardType

data class TypeSizeEntity(

    var id: Long,

    @field:TypeConverters(CardTypeValueConverter::class)
    var cardType: CardType = CardType.WORD,

    var cnt: Int = 0

) : BaseEntity()
