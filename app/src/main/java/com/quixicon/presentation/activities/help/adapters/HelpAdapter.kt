package com.quixicon.presentation.activities.help.adapters

import android.content.Context
import android.view.View
import com.quixicon.R
import com.quixicon.core.adapters.BindingAdapter
import com.quixicon.databinding.RowHelpPageBinding
import com.quixicon.presentation.activities.help.models.HelpPageModel

class HelpAdapter(
    context: Context,
    dataSource: List<HelpPageModel>,
    variableId: Int,
    layoutRes: Int,
    private val clickListenerVariableId: Int,
    val onItemClickListener: Listener
) : BindingAdapter<HelpPageModel, RowHelpPageBinding>(
    context,
    dataSource,
    variableId,
    layoutRes
),
    View.OnClickListener {

    override fun onBindViewHolder(holder: ItemViewHolder<RowHelpPageBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.binding?.setVariable(clickListenerVariableId, this)
    }

    override fun compareItems(i1: HelpPageModel, i2: HelpPageModel): Boolean {
        return false
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_left -> onItemClickListener.onFlipBack()
            R.id.iv_right -> onItemClickListener.onFlipForward()
        }
    }

    interface Listener {
        fun onFlipBack()
        fun onFlipForward()
    }
}
