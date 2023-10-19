package com.quixicon.presentation.activities.cards.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardAction(
    val action: CardActionType = CardActionType.COPY,
    val name: String = "default name",
    val id: Long? = null
) : Parcelable
