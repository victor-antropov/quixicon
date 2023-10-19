package com.quixicon.presentation.activities.cards.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.quixicon.presentation.activities.cards.models.BookCardModel
import com.quixicon.presentation.fragments.bookcard.views.BookCardFragment

/**
 * Adapter for [view_pager].
 */

class BookAdapter(
    fragmentActivity: FragmentActivity,
    val isSpeakerShown: Boolean
) : FragmentStateAdapter(fragmentActivity) {

    private val dataSource: ArrayList<BookCardModel> = ArrayList()

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun createFragment(position: Int): Fragment {
        dataSource.getOrNull(position)?.let {
            return BookCardFragment.createInstance(
                it.apply {
                    showSpeaker = isSpeakerShown
                }
            )
        } ?: throw Exception("no data for index $position")
    }

    override fun getItemId(position: Int): Long {
        val id = dataSource[position].hashCode().toLong()
        return id
    }

    override fun containsItem(itemId: Long): Boolean {
        return dataSource.any { it.hashCode().toLong() == itemId }
    }

    fun update(data: List<BookCardModel>) {
        dataSource.clear()
        dataSource.addAll(data)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): BookCardModel? {
        return dataSource.getOrNull(position)
    }

    fun updateKnowledge(item: BookCardModel, value: Int) {
        val position = dataSource.lastIndexOf(item)
        if (position != -1) {
            dataSource[position].knowledge = value
            notifyItemChanged(position)
        }
    }
}
