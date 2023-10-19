package com.quixicon.presentation.activities.collectionpreview.mappers

import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.remotes.CollectionData
import com.quixicon.domain.mappers.ListMapperImpl
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.collectionpreview.models.CollectionPreviewModel
import javax.inject.Inject

interface CollectionPreviewModelMapper : Mapper<Pair<CollectionData, Long>, CollectionPreviewModel>

class CollectionPreviewModelMapperImpl @Inject constructor(mapper: CardPreviewModelMapper) : CollectionPreviewModelMapper {

    private val listMapper = ListMapperImpl(mapper)

    override fun mapToInput(output: CollectionPreviewModel): Pair<CollectionData, Long> {
        TODO("Not yet implemented")
    }

    override fun mapToOutput(input: Pair<CollectionData, Long>): CollectionPreviewModel {
        return with(input) {
            val cards = first.cards?.let { listMapper.mapToOutput(it) } ?: arrayListOf()

            val qntPhrases = cards.filter {
                it.cardType == CardType.PHRASE
            }.size

            val qntRules = cards.filter {
                it.cardType == CardType.RULE
            }.size

            val qntWords = cards.filter {
                it.cardType == CardType.WORD
            }.size

            CollectionPreviewModel(
                name = first.name,
                description = first.description ?: "",
                installedCollectionId = second,
                qntWords = qntWords,
                qntRules = qntRules,
                qntPhrases = qntPhrases,
                cards = cards
            )
        }
    }
}
