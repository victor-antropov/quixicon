package com.quixicon.presentation.activities.social.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.quixicon.R
import com.quixicon.domain.entities.enums.SocialType
import java.io.Serializable

class SocialNetworkModel(
    val type: SocialType,
    val url: String
) : BaseObservable(), Serializable {

    @Bindable
    fun getIcon(): Int {
        return when (type) {
            SocialType.TWITTER -> R.drawable.ic_twitter
            SocialType.TELEGRAM -> R.drawable.ic_telegram_app
            SocialType.VK -> R.drawable.ic_vk
            SocialType.FACEBOOK -> R.drawable.ic_facebook
            SocialType.INSTAGRAM -> R.drawable.ic_instagram
        }
    }

    @Bindable
    fun getTitle(): String {
        return when (type) {
            SocialType.TWITTER -> "Twitter"
            SocialType.TELEGRAM -> "Telegram"
            SocialType.VK -> "VK"
            SocialType.FACEBOOK -> "Facebook"
            SocialType.INSTAGRAM -> "Instagram"
        }
    }
}
