package com.quixicon.presentation.activities.collectionpreview.adapters

import android.content.Context
import com.quixicon.core.adapters.BindingAdapter
import com.quixicon.databinding.RowCardPreviewBinding
import com.quixicon.presentation.activities.collectionpreview.models.CardPreviewModel

class CardsPreviewAdapter(
    context: Context,
    dataSource: List<CardPreviewModel>,
    variableId: Int,
    layoutRes: Int
) : BindingAdapter<CardPreviewModel, RowCardPreviewBinding>(
    context,
    dataSource,
    variableId,
    layoutRes
) {

    override fun compareItems(i1: CardPreviewModel, i2: CardPreviewModel): Boolean {
        return false
    }
}
