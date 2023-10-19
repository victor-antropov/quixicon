package com.quixicon.presentation.activities.collectionpreview.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.collectionpreview.models.CollectionPreviewModel

interface CollectionPreviewView {
    fun onBindCollection(args: EventArgs<CollectionPreviewModel>)
    fun onCollectionImported(args: EventArgs<Int>)
}
