package com.quixicon.presentation.activities.editcard.mappers

import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.db.CardExtraData
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.editcard.models.EditCardModel
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import javax.inject.Inject

interface EditCardModelMapper : Mapper<Card, EditCardModel>

class EditCardModelMapperImpl @Inject constructor() : EditCardModelMapper {

    override fun mapToInput(output: EditCardModel): Card {
        return object : Card {
            override val id: Long = output.id
            override var name: String? = output.original
            override var transcription: String? = output.transcription
            override var translation: String? = output.translation
            override var extraData: CardExtraData? = CardExtraData(output.description)
            override var cardType: CardType = output.cardType
            override var knowledge: Int? = null
            override var timestamp: OffsetDateTime? = OffsetDateTime.now(ZoneOffset.UTC)
            override var subject: LanguageCode? = null
            override var uid: Long? = null
        }
    }

    override fun mapToOutput(input: Card): EditCardModel {
        return with(input) {
            EditCardModel(
                id = this.id,
                original = this.name ?: "",
                transcription = this.transcription ?: "",
                description = this.extraData?.description ?: "",
                translation = this.translation ?: "",
                cardType = cardType
            )
        }
    }
}
