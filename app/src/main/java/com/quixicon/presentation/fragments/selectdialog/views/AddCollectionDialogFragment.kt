package com.quixicon.presentation.fragments.selectdialog.views
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quixicon.R
import com.quixicon.databinding.FragmentAddCollectionDialogBinding

class AddCollectionDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentAddCollectionDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_collection_dialog, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponents()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.item_server -> (activity as? View.OnClickListener)?.onClick(view)
            R.id.item_new -> (activity as? View.OnClickListener)?.onClick(view)
            R.id.item_file_icon -> (activity as? View.OnClickListener)?.onClick(view)
            R.id.item_file_text -> (activity as? View.OnClickListener)?.onClick(view)
            R.id.item_file_help -> (activity as? View.OnClickListener)?.onClick(view)
        }
        dismiss()
    }

    private fun initComponents() {
        binding.onClickListener = this
    }

    companion object {
        @JvmStatic
        fun createInstance() = AddCollectionDialogFragment()
    }
}
