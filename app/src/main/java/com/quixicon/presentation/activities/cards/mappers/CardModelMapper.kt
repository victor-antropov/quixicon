package com.quixicon.presentation.activities.cards.mappers

import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.cards.models.CardModel
import javax.inject.Inject

interface CardModelMapper : Mapper<Card, CardModel>

class CardModelMapperImpl @Inject constructor() : CardModelMapper {

    override fun mapToInput(output: CardModel): Card {
        TODO("Not yet implemented")
    }

    override fun mapToOutput(input: Card): CardModel {
        return with(input) {
            CardModel(
                id = this.id,
                name = this.name ?: "",
                transcription = this.transcription ?: "",
                description = this.extraData?.description ?: "",
                translation = this.translation ?: "",
                cardType = cardType,
                knowledge = this.knowledge,
                subject = subject?.value
            )
        }
    }
}
