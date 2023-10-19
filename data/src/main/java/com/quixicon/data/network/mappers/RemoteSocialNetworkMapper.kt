package com.quixicon.data.network.mappers

import com.quixicon.data.network.entities.RemoteSocialNetwork
import com.quixicon.domain.entities.remotes.SocialNetwork
import com.quixicon.domain.mappers.Mapper
import javax.inject.Inject

interface RemoteSocialNetworkMapper : Mapper<SocialNetwork, RemoteSocialNetwork>

class RemoteSocialNetworkMapperImpl @Inject constructor() : RemoteSocialNetworkMapper {

    override fun mapToInput(output: RemoteSocialNetwork): SocialNetwork {
        return with(output) {
            SocialNetwork(type, url, enable ?: true)
        }
    }

    override fun mapToOutput(input: SocialNetwork): RemoteSocialNetwork {
        return with(input) {
            RemoteSocialNetwork(type, url, enable)
        }
    }
}
