package com.quixicon.presentation.fragments.bookcard.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.book.BookEvent
import com.quixicon.databinding.FragmentBookCardBinding
import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.activities.cards.models.BookCardModel
import java.util.*

class BookCardFragment : Fragment(), View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private lateinit var model: BookCardModel

    private lateinit var binding: FragmentBookCardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_card, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            model = it.getSerializable(MODEL) as BookCardModel
            initComponents()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_left -> {
                (activity as? Listener)?.onFlipBack()
            }
            R.id.iv_right -> {
                (activity as? Listener)?.onFlipForward()
            }
            R.id.iv_card_menu -> {
                Metrics.send(requireContext(), BookEvent.MENU)
                showPopupMenu(view)
            }
            R.id.iv_speaker -> {
                (activity as? Listener)?.onSpeak(model.original)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy -> {
                (activity as? Listener)?.onActionSelected(model, CardActionType.COPY)
            }
            R.id.set_0 -> {
                (activity as? Listener)?.onActionSelected(model, CardActionType.SET_0)
            }
            R.id.set_100 -> {
                (activity as? Listener)?.onActionSelected(model, CardActionType.SET_100)
            }
            R.id.edit -> {
                (activity as? Listener)?.onActionSelected(model, CardActionType.EDIT)
            }
        }
        return true
    }

    private fun initComponents() {
        binding.cardModel = model
        binding.onClickListener = this

        with(resources.displayMetrics) {
            val height: Float = ((widthPixels / density) - 98) * density
            binding.layoutGroup.minHeight = height.toInt()

            if (model.answer.isNullOrEmpty()) {
                binding.layoutOriginal.minHeight = height.toInt()
            } else {
                binding.layoutOriginal.minHeight = (height * 0.6).toInt()
            }
        }
    }

    private fun showPopupMenu(view: View) {
        PopupMenu(context, view).apply {
            inflate(R.menu.menu_book_popup)
            setOnMenuItemClickListener(this@BookCardFragment)
            show()
        }
    }

    companion object {
        const val MODEL = "MODEL"

        @JvmStatic
        fun createInstance(model: BookCardModel) = BookCardFragment().apply {
            arguments = bundleOf(MODEL to model)
        }
    }

    interface Listener {
        fun onActionSelected(model: BookCardModel, action: CardActionType)
        fun onSpeak(string: String) {}
        fun onFlipBack()
        fun onFlipForward()
    }
}
