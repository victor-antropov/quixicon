package com.quixicon.presentation.fragments.test.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.review.ReviewEvent
import com.quixicon.core.support.extensions.FragmentExtensions.replaceFragment
import com.quixicon.core.support.extensions.FragmentExtensions.setTitle
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.databinding.FragmentTestResultBinding
import com.quixicon.presentation.fragments.review.views.ReviewFragment
import com.quixicon.presentation.fragments.test.models.TestResultModel

class TestResultFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentTestResultBinding

    private var correct: Int = 0
    private var wrong: Int = 0
    private var isIntro: Boolean = false
    private var showReview: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_result, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            correct = it.getInt(CORRECT)
            wrong = it.getInt(WRONG)
            isIntro = it.getBoolean(IS_INTRO, false)
            showReview = it.getBoolean(SHOW_REVIEW, false)
        }
        initComponents()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_continue -> {
                if (isIntro) {
                    (activity as? Listener)?.onLaunchMain()
                } else {
                    (activity as? Listener)?.onRepeatTest()
                }
            }

            R.id.btn_yes -> {
                Metrics.send(requireContext(), ReviewEvent.YES)
                (activity as? Listener)?.onReviewAction(true)
                removeFragment()
            }

            R.id.btn_no -> {
                Metrics.send(requireContext(), ReviewEvent.NO)
                (activity as? Listener)?.onReviewAction(false)
                removeFragment()
            }
        }
    }

    private fun initComponents() {
        setTitle(getString(R.string.test_result_caption))
        binding.model = TestResultModel(correct, wrong)

        if (isIntro) {
            binding.btnContinue.text = getString(R.string.intro_test_back)
            binding.tvInfo.changeAppearance(true)
        }

        binding.btnContinue.setOnClickListener(this)

        if (showReview) {
            Metrics.send(requireContext(), ReviewEvent.SHOW)
            replaceFragment(R.id.fragment_container, ReviewFragment.createInstance(), false)
        }
    }

    private fun removeFragment() {
        childFragmentManager.beginTransaction().apply {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            remove(childFragmentManager.findFragmentById(R.id.fragment_container)!!)
            commit()
        }
    }

    companion object {
        const val CORRECT = "CORRECT"
        const val WRONG = "WRONG"
        const val IS_INTRO = "IS_INTRO"
        const val SHOW_REVIEW = "SHOW_REVIEW"

        @JvmStatic
        fun createInstance(correct: Int, wrong: Int, isIntro: Boolean = false, showReview: Boolean = false) = TestResultFragment().apply {
            arguments = bundleOf(CORRECT to correct, WRONG to wrong, IS_INTRO to isIntro, SHOW_REVIEW to showReview)
        }
    }

    interface Listener {
        fun onRepeatTest()
        fun onLaunchMain()
        fun onReviewAction(isAgree: Boolean)
    }
}
