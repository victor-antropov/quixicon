package com.quixicon.presentation.activities.importcollection.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.importcollection.models.CollectionInfoModel

interface ImportView {
    fun onBindCollections(args: EventArgs<List<CollectionInfoModel>>)
    fun onCollectionImported(args: EventArgs<Int>)
    fun onCollectionDelete(args: EventArgs<Unit>)
}
