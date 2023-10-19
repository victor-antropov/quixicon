package com.quixicon.presentation.fragments.test.listeners

import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.fragments.test.entities.SwipeDirection

interface OnCardActionListener {
    fun onSwipe(direction: SwipeDirection)
    fun onActionSelected(action: CardActionType)
}
