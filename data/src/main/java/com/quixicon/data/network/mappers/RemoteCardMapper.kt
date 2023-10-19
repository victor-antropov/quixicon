package com.quixicon.data.network.mappers

import com.quixicon.data.network.entities.RemoteCardData
import com.quixicon.domain.entities.remotes.CardData
import com.quixicon.domain.mappers.Mapper
import javax.inject.Inject

interface RemoteCardMapper : Mapper<CardData, RemoteCardData>

class RemoteCardMapperImpl @Inject constructor() : RemoteCardMapper {

    override fun mapToInput(output: RemoteCardData): CardData {
        return with(output) {
            CardData(name, translation, description, transcription, cardType, knowledge, uid)
        }
    }

    override fun mapToOutput(input: CardData): RemoteCardData {
        return with(input) {
            RemoteCardData(name, translation, description, transcription, cardType, knowledge, uid)
        }
    }
}
