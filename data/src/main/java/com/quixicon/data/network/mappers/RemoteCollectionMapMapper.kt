package com.quixicon.data.network.mappers

import com.quixicon.data.network.entities.RemoteCollectionMap
import com.quixicon.domain.entities.remotes.CollectionMap
import com.quixicon.domain.mappers.Mapper
import javax.inject.Inject

interface RemoteCollectionMapMapper : Mapper<CollectionMap, RemoteCollectionMap>

class RemoteCollectionMapMapperImpl @Inject constructor() : RemoteCollectionMapMapper {

    override fun mapToInput(output: RemoteCollectionMap): CollectionMap {
        return with(output) {
            CollectionMap(id, url)
        }
    }

    override fun mapToOutput(input: CollectionMap): RemoteCollectionMap {
        return with(input) {
            RemoteCollectionMap(id, url)
        }
    }
}
