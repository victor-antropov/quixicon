package com.quixicon.presentation.activities.cards.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.models.CardModel

interface CardsView {
    fun onBindCards(args: EventArgs<List<CardModel>>)
    fun onBindCopyOptions(args: EventArgs<List<CardAction>>)
    fun onGetLinkedCollections(args: EventArgs<List<String>>)
    fun onCardDeleted(args: EventArgs<Unit>)
}
