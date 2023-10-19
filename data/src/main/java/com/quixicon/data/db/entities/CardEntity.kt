package com.quixicon.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.quixicon.data.db.converters.AnyBlobConverter
import com.quixicon.data.db.converters.CardTypeValueConverter
import com.quixicon.data.db.converters.DateTimeStringConverter
import com.quixicon.data.db.converters.LanguageCodeValueConverter
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.db.CardExtraData
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.LanguageCode
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "cards")
data class CardEntity(

    @field:PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    override var id: Long = 0,

    @field:ColumnInfo(name = "name")
    override var name: String? = null,

    @field:ColumnInfo(name = "transcription")
    override var transcription: String? = null,

    @field:ColumnInfo(name = "translation")
    override var translation: String? = null,

    @field:TypeConverters(AnyBlobConverter::class)
    @field:ColumnInfo(name = "extraData", typeAffinity = ColumnInfo.BLOB)
    override var extraData: CardExtraData? = null,

    @field:TypeConverters(CardTypeValueConverter::class)
    @field:ColumnInfo(name = "cardType")
    override var cardType: CardType = CardType.WORD,

    @field:ColumnInfo(name = "knowledge")
    override var knowledge: Int? = null,

    @field:TypeConverters(LanguageCodeValueConverter::class)
    @field:ColumnInfo(name = "subject")
    override var subject: LanguageCode? = null,

    @field:ColumnInfo(name = "uid")
    override var uid: Long? = null,

    @field:TypeConverters(DateTimeStringConverter::class)
    @field:ColumnInfo(name = "timestamp")
    override var timestamp: OffsetDateTime? = null

) : BaseEntity(), Card {

    constructor(card: Card) : this(
        id = card.id,
        name = card.name,
        transcription = card.transcription,
        translation = card.translation,
        extraData = card.extraData,
        cardType = card.cardType,
        knowledge = card.knowledge,
        subject = card.subject,
        uid = card.uid,
        timestamp = card.timestamp
    )
}
