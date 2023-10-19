package com.quixicon.presentation.fragments.test.views

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.quixicon.BuildConfig
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.install.InstallTestEvent
import com.quixicon.core.metrics.entities.test.TestEvent
import com.quixicon.core.support.extensions.FragmentExtensions.setTitle
import com.quixicon.databinding.FragmentTestProcessBinding
import com.quixicon.domain.entities.enums.TestDirection
import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.activities.cards.models.CardModel
import com.quixicon.presentation.activities.test.views.TestView
import com.quixicon.presentation.fragments.test.entities.AnimationType
import com.quixicon.presentation.fragments.test.entities.MarkType
import com.quixicon.presentation.fragments.test.entities.SwipeDirection
import com.quixicon.presentation.fragments.test.listeners.OnCardActionListener
import com.quixicon.presentation.fragments.test.models.TestCardModel
import com.quixicon.presentation.fragments.test.models.TestProcessModel
import timber.log.Timber
import java.io.File

class TestProcessFragment : Fragment(), OnCardActionListener, View.OnClickListener {

    private lateinit var binding: FragmentTestProcessBinding

    lateinit var model: TestProcessModel

    private val mHideHandler = Handler(Looper.getMainLooper())

    private var isDrawBlank = true

    private lateinit var testCardModel: TestCardModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_test_process, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            model = it.getSerializable(MODEL) as TestProcessModel
        }
        initComponents()
    }

    override fun onSwipe(direction: SwipeDirection) {
        when (direction) {
            SwipeDirection.LEFT -> {
                updateTestProcess(MarkType.WRONG)
                changeCard(AnimationType.LEFT)
                if ((activity as? TestView)?.isIntroState() == true) {
                    Metrics.send(
                        requireContext(),
                        InstallTestEvent.SWIPE_WRONG
                    )
                }
            }
            SwipeDirection.RIGHT -> {
                updateTestProcess(MarkType.CORRECT)
                changeCard(AnimationType.RIGHT)
                if ((activity as? TestView)?.isIntroState() == true) {
                    Metrics.send(
                        requireContext(),
                        InstallTestEvent.SWIPE_CORRECT
                    )
                }
            }
            SwipeDirection.UP -> {
                if (model.verticalSwipe) {
                    updateTestProcess(MarkType.KNOWN)
                    changeCard(AnimationType.UP)
                    Metrics.send(requireContext(), TestEvent.SWIPE_UP)
                } else if (model.showDrawButton) {
                    callDrawDialog()
                }
            }
            SwipeDirection.DOWN -> {
                if (model.verticalSwipe) {
                    updateTestProcess(MarkType.UNKNOWN)
                    changeCard(AnimationType.DOWN)
                    Metrics.send(requireContext(), TestEvent.SWIPE_DOWN)
                }
            }
        }
    }

    override fun onActionSelected(action: CardActionType) {
        when (action) {
            CardActionType.COPY -> {
                (activity as? Listener)?.copyCardIntent()
                Metrics.send(requireContext(), TestEvent.MENU_COPY)
            }
            CardActionType.SET_0 -> {
                updateTestProcess(MarkType.UNKNOWN)
                changeCard(AnimationType.DOWN)
                Metrics.send(requireContext(), TestEvent.MENU_0)
            }
            CardActionType.SET_100 -> {
                updateTestProcess(MarkType.KNOWN)
                changeCard(AnimationType.UP)
                Metrics.send(requireContext(), TestEvent.MENU_100)
            }
            else -> {
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_wrong -> {
                updateTestProcess(MarkType.WRONG)
                changeCard(AnimationType.LEFT)
                if ((activity as? TestView)?.isIntroState() == true) {
                    Metrics.send(
                        requireContext(),
                        InstallTestEvent.BTN_WRONG
                    )
                }
            }
            R.id.btn_correct -> {
                updateTestProcess(MarkType.CORRECT)
                changeCard(AnimationType.RIGHT)
                if ((activity as? TestView)?.isIntroState() == true) {
                    Metrics.send(
                        requireContext(),
                        InstallTestEvent.BTN_CORRECT
                    )
                }
            }
            R.id.iv_speaker -> {
                Timber.e("Manual Speaker")
                (activity as? Listener)?.run {
                    getCardModel(model.position)?.let {
                        onSpeak(it.name, showErrors = true)
                    }
                }
            }
            R.id.btn_draw -> {
                callDrawDialog()
            }
            R.id.layout_draw_preview -> {
                Timber.e("Click on Preview")
                callDrawDialog()
            }
        }
    }

    private fun initComponents() {
        setTitle(getString(R.string.action_tests))
        changeCard(AnimationType.SHOW)
        binding.onClickListener = this
        binding.model = model
    }

    private fun changeCard(type: AnimationType) {
        isDrawBlank = true
        hideDrawPreview()

        when (type) {
            AnimationType.RIGHT -> R.attr.colorTestBgRight
            AnimationType.LEFT -> R.attr.colorTestBgLeft
            AnimationType.UP -> R.attr.colorTestBgUp
            AnimationType.DOWN -> R.attr.colorTestBgDown
            else -> null
        }?.run {
            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(this, typedValue, true)
            binding.fragmentInnerContainer.setBackgroundColor(typedValue.data)
            mHideHandler.postDelayed({
                binding.fragmentInnerContainer.setBackgroundResource(R.color.transparent)
            }, 500)
        }

        (activity as? Listener)?.getCardModel(model.position)?.let {
            var reverse = false

            if (model.testDirection == TestDirection.INVERTED) reverse = true
            if (model.testDirection == TestDirection.MIXED) {
                if (model.position % 2 == 1) {
                    if (Math.random() < 0.5) reverse = true
                } else {
                    if (Math.random() > 0.5) reverse = true
                }
            }

            if (!reverse && model.playQuestions) (activity as? Listener)?.onSpeak(it.name, showErrors = false)

            testCardModel = TestCardModel(
                id = it.id,
                original = it.name,
                transcription = it.transcription,
                description = it.description,
                answer = it.translation,
                reverseDirection = reverse,
                size = model.size,
                position = model.position + 1,
                qntCorrect = model.qntCorrect,
                qntWrong = model.qntWrong,
                showTranscription = model.showTranscription,
                ttsAvailable = model.ttsAvailable
            )

            childFragmentManager.beginTransaction().apply {
                setAnimation(type)
                replace(
                    R.id.fragment_inner_container,
                    TestCardFragment.createInstance(testCardModel)
                )
                commit()
            }
        } ?: run {
            childFragmentManager.beginTransaction().apply {
                setAnimation(type)
                remove(childFragmentManager.findFragmentById(R.id.fragment_inner_container)!!)
                commit()
            }
            (activity as? Listener)?.onTestFinish(model.qntCorrect, model.qntWrong)
        }
    }

    private fun updateTestProcess(mark: MarkType) {
        (activity as? Listener)?.run {
            getCardModel(model.position)?.let {
                var knowledge = it.knowledge ?: 0
                when (mark) {
                    MarkType.CORRECT -> knowledge += 10
                    MarkType.WRONG -> knowledge -= 20
                    MarkType.KNOWN -> knowledge = 100
                    MarkType.UNKNOWN -> knowledge = 0
                }
                if (knowledge > 100) knowledge = 100
                if (knowledge < 0) knowledge = 0
                this.setCardKnowledge(model.position, knowledge)
            }
        }

        if ((activity as? TestView)?.isIntroState() == true) {
            val cardNumberEvent = when (model.position) {
                0 -> InstallTestEvent.CARD_1
                1 -> InstallTestEvent.CARD_2
                2 -> InstallTestEvent.CARD_3
                3 -> InstallTestEvent.CARD_4
                else -> null
            }
            cardNumberEvent?.let {
                Metrics.send(requireContext(), it)
            }
        }
        //
        model.position++
        if (mark == MarkType.CORRECT || mark == MarkType.KNOWN) {
            model.qntCorrect++
        } else {
            model.qntWrong++
        }
    }

    fun abort() {
        if ((activity as? TestView)?.isIntroState() == true) {
            Metrics.send(
                requireContext(),
                InstallTestEvent.ABORT
            )
        } else {
            Metrics.send(requireContext(), TestEvent.ABORT)
        }
        (activity as? Listener)?.onTestFinish(model.qntCorrect, model.qntWrong)
    }

    fun callDrawDialog() {
        // Open draw dialog
        val original = if (testCardModel.reverseDirection) "" else testCardModel.original
        val translation = if (testCardModel.reverseDirection) testCardModel.answer ?: "" else ""
        (activity as? Listener)?.showDrawDialog(original, translation, !isDrawBlank)
    }

    fun updateDrawPreview(isVisible: Boolean) {
        isDrawBlank = !isVisible
        model.showDrawPreview = isVisible
        binding.model = model

        if (isVisible) {
            val fileName = BuildConfig.DRAW_FILE_NAME
            val file = File(requireContext().getExternalFilesDir(null)?.absolutePath, fileName)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.ivPreview.setImageBitmap(bitmap)
            }
        }
    }

    private fun hideDrawPreview() {
        model.showDrawPreview = false
        binding.model = model
    }

    companion object {
        const val MODEL = "MODEL"

        @JvmStatic
        fun createInstance(model: TestProcessModel) = TestProcessFragment().apply {
            arguments = bundleOf(MODEL to model)
        }
    }

    interface Listener {
        fun getCardModel(position: Int): CardModel?
        fun setCardKnowledge(position: Int, value: Int): Boolean
        fun onTestFinish(correct: Int, wrong: Int)
        fun copyCardIntent()
        fun onSpeak(string: String, showErrors: Boolean) {}
        fun showDrawDialog(hintOriginal: String, hintTranslation: String, restore: Boolean)
    }
}

fun FragmentTransaction.setAnimation(animationType: AnimationType) {
    when (animationType) {
        AnimationType.LEFT -> setCustomAnimations(
            R.anim.enter_from_behind,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        AnimationType.RIGHT -> setCustomAnimations(
            R.anim.enter_from_behind,
            R.anim.exit_to_right,
            R.anim.enter_from_right,
            R.anim.exit_to_left
        )
        AnimationType.UP -> setCustomAnimations(
            R.anim.enter_from_behind,
            R.anim.exit_to_top,
            R.anim.enter_from_top,
            R.anim.exit_to_bottom
        )
        AnimationType.DOWN -> setCustomAnimations(
            R.anim.enter_from_behind,
            R.anim.exit_to_bottom,
            R.anim.enter_from_right,
            R.anim.exit_to_left
        )
        AnimationType.SHOW -> setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        AnimationType.HIDE -> setCustomAnimations(
            R.anim.enter_from_bottom,
            R.anim.exit_to_top,
            R.anim.enter_from_top,
            R.anim.exit_to_bottom
        )
    }
}
