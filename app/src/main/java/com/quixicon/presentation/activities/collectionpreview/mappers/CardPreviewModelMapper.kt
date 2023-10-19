package com.quixicon.presentation.activities.collectionpreview.mappers

import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.remotes.CardData
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.collectionpreview.models.CardPreviewModel
import javax.inject.Inject

interface CardPreviewModelMapper : Mapper<CardData, CardPreviewModel>

class CardPreviewModelMapperImpl @Inject constructor() : CardPreviewModelMapper {

    override fun mapToInput(output: CardPreviewModel): CardData {
        TODO("Not yet implemented")
    }

    override fun mapToOutput(input: CardData): CardPreviewModel {
        return with(input) {
            CardPreviewModel(
                name = this.name,
                transcription = this.transcription ?: "",
                description = this.description ?: "",
                translation = this.translation ?: "",
                cardType = cardType ?: CardType.WORD
            )
        }
    }
}
