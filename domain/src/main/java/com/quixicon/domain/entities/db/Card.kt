package com.quixicon.domain.entities.db

import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.LanguageCode
import org.threeten.bp.OffsetDateTime

interface Card {
    val id: Long
    var name: String?
    var transcription: String?
    var translation: String?
    var extraData: CardExtraData?
    var cardType: CardType
    var knowledge: Int?
    var timestamp: OffsetDateTime?
    var subject: LanguageCode?
    var uid: Long?
}
