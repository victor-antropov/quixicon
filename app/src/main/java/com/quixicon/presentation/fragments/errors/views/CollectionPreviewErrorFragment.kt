package com.quixicon.presentation.fragments.errors.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.quixicon.R
import com.quixicon.databinding.FragmentCollectionPreviewErrorBinding

class CollectionPreviewErrorFragment private constructor() : Fragment() {

    private lateinit var binding: FragmentCollectionPreviewErrorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_collection_preview_error,
                container,
                false
            )
        return binding.root
    }

    companion object {
        @JvmStatic
        fun createInstance() = CollectionPreviewErrorFragment()
    }
}
