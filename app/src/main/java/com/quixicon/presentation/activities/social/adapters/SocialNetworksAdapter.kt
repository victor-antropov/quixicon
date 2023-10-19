package com.quixicon.presentation.activities.social.adapters

import android.content.Context
import android.view.View
import com.quixicon.core.adapters.InteractiveAdapter
import com.quixicon.databinding.RowSocialNetworkBinding
import com.quixicon.presentation.activities.social.models.SocialNetworkModel

class SocialNetworksAdapter(
    context: Context,
    dataSource: List<SocialNetworkModel>,
    variableId: Int,
    layoutRes: Int,
    clickListenerVariableId: Int,
    onItemClickListener: OnItemClickListener
) : InteractiveAdapter<SocialNetworkModel, RowSocialNetworkBinding>(
    context,
    dataSource,
    variableId,
    layoutRes,
    clickListenerVariableId,
    onItemClickListener
),
    View.OnClickListener {

    override fun compareItems(i1: SocialNetworkModel, i2: SocialNetworkModel): Boolean {
        return false
    }

    override fun getVariableFromBinding(binding: RowSocialNetworkBinding?): SocialNetworkModel? {
        return binding?.model
    }

    interface OnItemClickListener :
        InteractiveAdapter.OnItemClickListener<SocialNetworkModel, RowSocialNetworkBinding>
}
