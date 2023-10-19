package com.quixicon.presentation.activities.importcollection.mappers

import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionInfo
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.importcollection.models.CollectionInfoModel
import javax.inject.Inject

interface CollectionInfoModelMapper : Mapper<Pair<CollectionInfo, Long>, CollectionInfoModel>

class CollectionInfoModelMapperImpl @Inject constructor() : CollectionInfoModelMapper {

    override fun mapToInput(output: CollectionInfoModel): Pair<CollectionInfo, Long> {
        TODO("Not yet implemented")
    }

    override fun mapToOutput(input: Pair<CollectionInfo, Long>): CollectionInfoModel {
        return with(input) {
            CollectionInfoModel(
                name = first.name,
                description = first.description ?: "",
                cntPhrases = first.cntPhrases,
                cntRules = first.cntRules,
                cntWords = first.cntWords,
                imei = first.imei,
                link = first.link,
                isInstalled = second != 0L,
                id = second,
                codeSubject = first.codeSubject ?: LanguageCode.UNDEFINED,
                codeStudent = first.codeStudent ?: LanguageCode.UNDEFINED
            )
        }
    }
}
