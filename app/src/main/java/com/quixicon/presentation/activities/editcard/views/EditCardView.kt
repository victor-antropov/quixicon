package com.quixicon.presentation.activities.editcard.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.editcard.models.EditCardModel

interface EditCardView {
    fun onBindCard(args: EventArgs<EditCardModel>)
    fun onUpdateCard(args: EventArgs<Unit>)
}
