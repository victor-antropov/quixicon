package com.quixicon.presentation.fragments.review.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.quixicon.R
import com.quixicon.databinding.FragmentReviewBinding

class ReviewFragment private constructor() : Fragment() {

    private lateinit var binding: FragmentReviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_review,
                container,
                false
            )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initComponents()
    }

    fun initComponents() {
        binding.btnNo.setOnClickListener {
            (parentFragment as? View.OnClickListener)?.onClick(it)
        }

        binding.btnYes.setOnClickListener {
            (parentFragment as? View.OnClickListener)?.onClick(it)
        }
    }

    companion object {
        @JvmStatic
        fun createInstance() = ReviewFragment()
    }
}
