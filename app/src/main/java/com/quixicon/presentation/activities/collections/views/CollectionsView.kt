package com.quixicon.presentation.activities.collections.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.collections.models.CollectionModel

interface CollectionsView {
    fun onBindCollections(args: EventArgs<List<CollectionModel>>)
    fun onCollectionDelete(args: EventArgs<Unit>)
    fun onGetExportData(args: EventArgs<ByteArray>)
    fun onCollectionImported(args: EventArgs<Unit>)
}
