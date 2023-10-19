package com.quixicon.presentation.activities.test.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.models.CardModel
import com.quixicon.presentation.activities.test.models.TestCollectionModel

interface TestView {
    fun onBindCards(args: EventArgs<List<CardModel>>)
    fun onBindCollections(args: EventArgs<List<TestCollectionModel>>)
    fun onBindCopyOptions(args: EventArgs<List<CardAction>>)
    fun onCopyCard(args: EventArgs<Unit>)
    fun isIntroState(): Boolean = false
}
