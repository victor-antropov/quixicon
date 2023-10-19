package com.quixicon.presentation.activities.importcollection.adapters

import android.content.Context
import android.view.View
import com.quixicon.core.adapters.InteractiveAdapter
import com.quixicon.databinding.RowCollectionInfoBinding
import com.quixicon.presentation.activities.importcollection.models.CollectionInfoModel

class CollectionsInfoAdapter(
    context: Context,
    dataSource: List<CollectionInfoModel>,
    variableId: Int,
    layoutRes: Int,
    clickListenerVariableId: Int,
    onItemClickListener: OnItemClickListener,
    longClickListenerVariableId: Int? = null
) : InteractiveAdapter<CollectionInfoModel, RowCollectionInfoBinding>(
    context,
    dataSource,
    variableId,
    layoutRes,
    clickListenerVariableId,
    onItemClickListener,
    longClickListenerVariableId
),
    View.OnClickListener {

    override fun compareItems(i1: CollectionInfoModel, i2: CollectionInfoModel): Boolean {
        return false
    }

    override fun getVariableFromBinding(binding: RowCollectionInfoBinding?): CollectionInfoModel? {
        return binding?.collectionModel
    }

    interface OnItemClickListener :
        InteractiveAdapter.OnItemClickListener<CollectionInfoModel, RowCollectionInfoBinding>

    fun setInstalled(item: CollectionInfoModel, state: Boolean) {
        getPositionOrNull(item)?.let {
            updateItem(it, item.apply { isInstalled = state })
        }
    }
}
