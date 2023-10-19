package com.quixicon.presentation.activities.cards.mappers

import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.cards.entities.CardAction
import javax.inject.Inject

interface CardActionMapper : Mapper<Collection, CardAction>

class CardActionMapperImpl @Inject constructor() : CardActionMapper {

    override fun mapToInput(output: CardAction): Collection {
        TODO("Not yet implemented")
    }

    override fun mapToOutput(input: Collection): CardAction {
        return with(input) {
            CardAction(
                id = this.id,
                name = this.name ?: ""
            )
        }
    }
}
