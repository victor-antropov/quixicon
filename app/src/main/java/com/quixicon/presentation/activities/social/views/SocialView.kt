package com.quixicon.presentation.activities.social.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.social.models.SocialNetworkModel

interface SocialView {
    fun onBindSocialNetworks(args: EventArgs<List<SocialNetworkModel>>)
}
