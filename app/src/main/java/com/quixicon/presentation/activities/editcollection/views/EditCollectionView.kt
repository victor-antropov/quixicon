package com.quixicon.presentation.activities.editcollection.views

import com.quixicon.core.system.EventArgs
import com.quixicon.presentation.activities.editcollection.models.EditCollectionModel

interface EditCollectionView {
    fun onBindCollection(args: EventArgs<EditCollectionModel>)
    fun onUpdateCollection(args: EventArgs<Unit>)
}
