package com.quixicon.domain.entities.presentation

import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.db.CardExtraData
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.LanguageCode
import org.threeten.bp.OffsetDateTime

data class CardItem(

    override val id: Long,

    override var name: String? = null,

    override var transcription: String? = null,

    override var translation: String? = null,

    override var extraData: CardExtraData? = null,

    override var cardType: CardType = CardType.WORD,

    override var knowledge: Int? = null,

    override var subject: LanguageCode? = null,

    override var uid: Long? = null,

    override var timestamp: OffsetDateTime? = null

) : Card
