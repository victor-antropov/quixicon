package com.quixicon.presentation.activities.cards.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.models.BookCardModel

interface BookView {

    fun onBindBookCards(args: EventArgs<List<BookCardModel>>)
    fun onBindCopyOptions(args: EventArgs<List<CardAction>>)
    fun onCopyCard(args: EventArgs<Unit>)
    fun onUpdateCard(args: EventArgs<Unit>)
}
