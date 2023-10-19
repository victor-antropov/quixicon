package com.quixicon.presentation.activities.collections.adapters

import android.content.Context
import android.view.View
import com.quixicon.core.adapters.InteractiveAdapter
import com.quixicon.databinding.RowCollectionBinding
import com.quixicon.presentation.activities.collections.models.CollectionModel

class CollectionsAdapter(
    context: Context,
    dataSource: List<CollectionModel>,
    variableId: Int,
    layoutRes: Int,
    clickListenerVariableId: Int,
    onItemClickListener: OnItemClickListener,
    longClickListenerVariableId: Int? = null
) : InteractiveAdapter<CollectionModel, RowCollectionBinding>(
    context,
    dataSource,
    variableId,
    layoutRes,
    clickListenerVariableId,
    onItemClickListener,
    longClickListenerVariableId
),
    View.OnClickListener {

    override fun compareItems(i1: CollectionModel, i2: CollectionModel): Boolean {
        return false
    }

    override fun getVariableFromBinding(binding: RowCollectionBinding?): CollectionModel? {
        return binding?.collectionModel
    }

    interface OnItemClickListener :
        InteractiveAdapter.OnItemClickListener<CollectionModel, RowCollectionBinding>
}
