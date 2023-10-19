package com.quixicon.data.network.mappers

import com.quixicon.data.network.entities.RemoteCollectionData
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.remotes.CollectionData
import com.quixicon.domain.mappers.ListMapperImpl
import com.quixicon.domain.mappers.Mapper
import javax.inject.Inject

interface RemoteCollectionMapper : Mapper<CollectionData, RemoteCollectionData>

class RemoteCollectionMapperImpl @Inject constructor(private val mapper: RemoteCardMapper) : RemoteCollectionMapper {

    private val listMapper = ListMapperImpl(mapper)

    override fun mapToInput(output: RemoteCollectionData): CollectionData {
        return with(output) {
            CollectionData(
                name,
                description,
                codeSubject,
                codeStudent,
                imei,
                cards?.let { listMapper.mapToInput(it) },
                collectionType ?: CollectionType.DEFAULT
            )
        }
    }

    override fun mapToOutput(input: CollectionData): RemoteCollectionData {
        return with(input) {
            RemoteCollectionData(
                name,
                description,
                codeSubject,
                codeStudent,
                imei,
                cards?.let { listMapper.mapToOutput(it) },
                collectionType
            )
        }
    }
}
