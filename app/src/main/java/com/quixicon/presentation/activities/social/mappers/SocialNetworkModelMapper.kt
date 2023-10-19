package com.quixicon.presentation.activities.social.mappers

import com.quixicon.domain.entities.remotes.SocialNetwork
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.social.models.SocialNetworkModel
import javax.inject.Inject

interface SocialNetworkModelMapper : Mapper<SocialNetwork, SocialNetworkModel>

class SocialNetworkModelMapperImpl @Inject constructor() : SocialNetworkModelMapper {

    override fun mapToInput(output: SocialNetworkModel): SocialNetwork {
        TODO("Not yet implemented")
    }

    override fun mapToOutput(input: SocialNetwork): SocialNetworkModel {
        return with(input) {
            SocialNetworkModel(
                type,
                url
            )
        }
    }
}
