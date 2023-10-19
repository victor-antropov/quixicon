package com.quixicon.presentation.activities.cards.adapters

import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.quixicon.core.adapters.BindingAdapter
import com.quixicon.databinding.RowCardBinding
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.activities.cards.entities.CardSwipeMode
import com.quixicon.presentation.activities.cards.helpers.CardTouchHelperAdapter
import com.quixicon.presentation.activities.cards.models.CardEditModel
import com.quixicon.presentation.activities.cards.models.CardModel

class CardsExtendedAdapter(
    context: Context,
    dataSource: List<CardModel>,
    variableId: Int,
    layoutRes: Int,
    private val clickListenerVariableId: Int,
    private val onItemActionListener: OnItemActionListener,
    private val editModeVariableId: Int,
    private val longClickListenerVariableId: Int? = null,
    var typeFilter: MutableMap<CardType, Boolean> = mutableMapOf(CardType.WORD to true, CardType.PHRASE to true, CardType.RULE to true),
    var showLanguage: Boolean = true
) : BindingAdapter<CardModel, RowCardBinding>(
    context,
    dataSource,
    variableId,
    layoutRes
),
    View.OnClickListener,
    CardTouchHelperAdapter,
    View.OnLongClickListener {

    var editMode: CardSwipeMode = CardSwipeMode.DISABLED
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var actionType = CardActionType.SET_100

    fun getVariableFromBinding(binding: RowCardBinding?): CardModel? {
        return binding?.cardModel
    }

    override fun onBindViewHolder(holder: ItemViewHolder<RowCardBinding>, position: Int) {
        holder.binding?.setVariable(clickListenerVariableId, this)
        holder.binding?.setVariable(editModeVariableId, CardEditModel(editMode, showLanguage))
        longClickListenerVariableId?.let {
            holder.binding?.setVariable(it, this)
        }
        super.onBindViewHolder(holder, position)
    }

    override fun onClick(v: View) {
        val binding: RowCardBinding? = DataBindingUtil.findBinding(v)
        val variable = getVariableFromBinding(binding)
        onItemActionListener.onItemClick(variable, binding)
    }

    override fun onLongClick(v: View): Boolean {
        val binding: RowCardBinding? = DataBindingUtil.findBinding(v)
        val variable = getVariableFromBinding(binding)
        onItemActionListener.onItemLongClick(variable, binding)
        return true
    }

    override fun compareItems(i1: CardModel, i2: CardModel): Boolean {
        return false
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int, direction: Int) {
        val card = getItem(position)
        if (direction == ItemTouchHelper.START) {
            onItemActionListener.onItemSwipe(card, ItemTouchHelper.START)
            when (actionType) {
                CardActionType.SET_100 -> updateKnowledge(position, 100)
                CardActionType.SET_0 -> updateKnowledge(position, 0)
                CardActionType.DELETE -> removeItem(position)
                else -> {}
            }
        } else {
            onItemActionListener.onItemSwipe(card, ItemTouchHelper.END)
            setCopied(position)
        }
    }

    override fun filterItem(item: CardModel, text: String): Boolean {
        var result = typeFilter[item.cardType] == true
        if (text.isNotEmpty()) {
            result = result && (item.name.contains(text, true) || item.translation.contains(text, true))
        }
        return result
    }

    fun updateKnowledge(position: Int, value: Int) {
        val card = getItem(position).apply {
            knowledge = value
        }
        updateItem(position, card)
    }

    fun updateKnowledge(item: CardModel, value: Int) {
        getPositionOrNull(item)?.let {
            updateItem(it, item.apply { knowledge = value })
        }
    }

    fun removeCardItem(item: CardModel) {
        getPositionOrNull(item)?.let {
            removeItem(it)
        }
    }

    private fun setCopied(position: Int) {
        val card = getItem(position).apply {
            copyFlag = true
        }
        updateItem(position, card)
    }

    interface OnItemActionListener {
        fun onItemClick(variable: CardModel?, binding: RowCardBinding?)
        fun onItemLongClick(variable: CardModel?, binding: RowCardBinding?) {}
        fun onItemSwipe(variable: CardModel?, direction: Int)
    }
}
