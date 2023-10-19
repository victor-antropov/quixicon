package com.quixicon.presentation.fragments.test.views

import android.annotation.SuppressLint
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
import com.quixicon.core.metrics.entities.install.InstallTestEvent
import com.quixicon.core.metrics.entities.test.TestEvent
import com.quixicon.databinding.FragmentTestCardBinding
import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.activities.test.views.TestView
import com.quixicon.presentation.fragments.test.entities.SwipeDirection
import com.quixicon.presentation.fragments.test.listeners.OnCardActionListener
import com.quixicon.presentation.fragments.test.listeners.OnSwipeTouchListener
import com.quixicon.presentation.fragments.test.models.TestCardModel
import java.util.*

class TestCardFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var model: TestCardModel

    private lateinit var binding: FragmentTestCardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_card, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            model = it.getSerializable(MODEL) as TestCardModel
        }
        initComponents()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy -> (parentFragment as? OnCardActionListener)?.onActionSelected(CardActionType.COPY)
            R.id.set_0 -> (parentFragment as? OnCardActionListener)?.onActionSelected(CardActionType.SET_0)
            R.id.set_100 -> (parentFragment as? OnCardActionListener)?.onActionSelected(
                CardActionType.SET_100
            )
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initComponents() {
        binding.cardModel = model

        binding.layoutGroup.setOnTouchListener(
            object : OnSwipeTouchListener(requireContext()) {
                override fun onSwipeRight() {
                    (parentFragment as? OnCardActionListener)?.onSwipe(SwipeDirection.RIGHT)
                }

                override fun onSwipeLeft() {
                    (parentFragment as? OnCardActionListener)?.onSwipe(SwipeDirection.LEFT)
                }

                override fun onSwipeTop() {
                    (parentFragment as? OnCardActionListener)?.onSwipe(SwipeDirection.UP)
                }

                override fun onSwipeBottom() {
                    (parentFragment as? OnCardActionListener)?.onSwipe(SwipeDirection.DOWN)
                }

                override fun onClick() {
                    binding.cardModel?.opened = true
                    if ((activity as? TestView)?.isIntroState() == true) Metrics.send(requireContext(), InstallTestEvent.SHOW_ANSWER)
                }
            }
        )

        binding.ivCardMenu.setOnClickListener { view ->
            Metrics.send(requireContext(), TestEvent.MENU)
            showPopupMenu(view)
        }

        binding.ivSpeaker.setOnClickListener {
            (parentFragment as? View.OnClickListener)?.onClick(it)
        }

        with(resources.displayMetrics) {
            val height: Float = ((widthPixels / density) - 98) * density
            binding.layoutGroup.minHeight = height.toInt()
            binding.layoutOriginal.minHeight = (height * 0.6).toInt()
        }
    }

    private fun showPopupMenu(view: View) {
        PopupMenu(context, view).apply {
            inflate(R.menu.menu_test_popup)
            setOnMenuItemClickListener(this@TestCardFragment)
            show()
        }
    }

    companion object {
        const val MODEL = "MODEL"

        @JvmStatic
        fun createInstance(model: TestCardModel) = TestCardFragment().apply {
            arguments = bundleOf(MODEL to model)
        }
    }
}
