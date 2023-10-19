package com.quixicon.presentation.fragments.infodialog.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quixicon.R
import com.quixicon.core.support.helpers.HtmlHelper

class InfoDialogFragment : DialogFragment() {

    var created = false

    var resId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        created = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.fragment_info_dialog, null)

        with(view) {
            val textView = findViewById<AppCompatTextView>(R.id.tv_info)

            HtmlHelper.setHtmlWithLinkClickHandler(textView, getString(resId), requireContext())

            findViewById<View>(R.id.btn_ok).setOnClickListener {
                dismiss()
            }

            findViewById<View>(R.id.btn_close).setOnClickListener {
                dismiss()
            }
        }

        return MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setView(view)
            .setCancelable(false)
            .create().apply {
                window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        created = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    /**
     * Restores properties from the bundle instance.
     *
     * @param savedInstanceState Saved state of current fragment dialog.
     */
    private fun restoreProperties(savedInstanceState: Bundle) {
        with(savedInstanceState) {
            resId = getInt(RES_ID, 0)
        }
    }

    /**
     * Restores state of fragment dialog.
     *
     * @param savedInstanceState Saved state of current fragment dialog.
     */
    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also { restoreProperties(it) } ?: run {
            arguments?.also { restoreProperties(it) }
        }
    }

    /**
     * Saves current state of fragment dialog.
     *
     * @param outState Parameter for store current fragment dialog state.
     */
    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putInt(RES_ID, resId)
        }
    }

    companion object {
        const val RES_ID = "RES_ID"

        @JvmStatic
        fun createInstance(
            resId: Int
        ) = InfoDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(RES_ID, resId)
            }
        }
    }
}
