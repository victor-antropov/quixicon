package com.quixicon.data.network.mappers

import com.quixicon.data.network.entities.RemoteCollectionInfo
import com.quixicon.domain.entities.remotes.CollectionInfo
import com.quixicon.domain.mappers.Mapper
import javax.inject.Inject

interface RemoteCollectionInfoMapper : Mapper<CollectionInfo, RemoteCollectionInfo>

class RemoteCollectionInfoMapperImpl @Inject constructor() : RemoteCollectionInfoMapper {

    override fun mapToInput(output: RemoteCollectionInfo): CollectionInfo {
        return with(output) {
            CollectionInfo(name, description, codeSubject, codeStudent, imei, link, cntWords, cntPhrases, cntRules)
        }
    }

    override fun mapToOutput(input: CollectionInfo): RemoteCollectionInfo {
        return with(input) {
            RemoteCollectionInfo(name, description, codeSubject, codeStudent, imei, link, cntWords, cntPhrases, cntRules)
        }
    }
}
