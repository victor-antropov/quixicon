package com.quixicon.presentation.fragments.test.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.quixicon.R
import com.quixicon.core.support.helpers.HtmlHelper
import com.quixicon.databinding.FragmentTestSettingsIntroBinding
import com.quixicon.presentation.fragments.test.listeners.TestSettingsListener
import com.quixicon.presentation.fragments.test.models.TestSettingsModel
import timber.log.Timber

class TestSettingsIntroFragment : Fragment() {

    private lateinit var binding: FragmentTestSettingsIntroBinding

    private lateinit var model: TestSettingsModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.e("Create Settings Intro Fragment")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_settings_intro, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            model = it.getSerializable(MODEL) as TestSettingsModel
            initComponents()
            Timber.e("Settings Fragment created: $model")
        }
    }

    override fun onDestroy() {
        Timber.e("Destroy Settings Fragment")
        super.onDestroy()
    }

    private fun initComponents() {
        binding.btnStart.setOnClickListener {
            startTest()
        }
        HtmlHelper.setHtmlWithLinkClickHandler(binding.tvDescription, getString(R.string.intro_test), requireContext())
    }

    private fun startTest() {
        (activity as? TestSettingsListener)?.onTestSettingsReady(model)
    }

    companion object {
        const val MODEL = "MODEL"

        @JvmStatic
        fun createInstance(model: TestSettingsModel) = TestSettingsIntroFragment().apply {
            arguments = bundleOf(MODEL to model)
        }
    }
}
