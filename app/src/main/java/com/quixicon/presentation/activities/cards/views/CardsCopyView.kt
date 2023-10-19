package com.quixicon.presentation.activities.cards.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.cards.models.CardModel

interface CardsCopyView {
    fun onBindCards(args: EventArgs<List<CardModel>>)
}
