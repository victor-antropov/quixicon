package com.quixicon.presentation.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.quixicon.R
import com.quixicon.databinding.FragmentConnectionErrorBinding
import com.quixicon.presentation.activities.base.DaggerInternetActivity

class ConnectionErrorFragment private constructor() : Fragment() {

    private lateinit var binding: FragmentConnectionErrorBinding
    private var serverError: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_connection_error,
                container,
                false,
            )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)
        initComponents()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putBoolean(Argument.IS_SERVER_ERROR.name, serverError)
        }
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            serverError = it.getBoolean(Argument.IS_SERVER_ERROR.name, false)
        } ?: run {
            arguments?.also {
                serverError = it.getBoolean(Argument.IS_SERVER_ERROR.name, false)
            }
        }
    }

    private fun initComponents() {
        binding.tvTitle.text = if (serverError) {
            getString(R.string.import_error_server)
        } else {
            getString(R.string.import_error_connection)
        }

        binding.btnRepeatInternetRequest.setOnClickListener {
            invokeLatestRequest()
        }
    }

    private fun invokeLatestRequest() =
        if (arguments?.getBoolean(Argument.IS_ACTIVITY.name) == true) {
            val activity = requireActivity() as DaggerInternetActivity
            activity.supportFragmentManager.beginTransaction().remove(this).commitNow()
            activity.invokeLatestRequest()
        } else {
            throw IllegalArgumentException("Not yet implemented")
        }

    private enum class Argument {
        IS_ACTIVITY,
        IS_SERVER_ERROR,
    }

    companion object {
        @JvmStatic
        fun createInstance(isActivity: Boolean, isServerError: Boolean = false) =
            ConnectionErrorFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(Argument.IS_ACTIVITY.name, isActivity)
                    putBoolean(Argument.IS_SERVER_ERROR.name, isServerError)
                }
            }
    }
}
