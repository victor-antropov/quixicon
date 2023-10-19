package com.quixicon.presentation.activities.start.views

import com.quixicon.core.system.EventArgs

interface StartView {

    fun onMultiCollectionsImportedSuccess(args: EventArgs<Int>)
}
