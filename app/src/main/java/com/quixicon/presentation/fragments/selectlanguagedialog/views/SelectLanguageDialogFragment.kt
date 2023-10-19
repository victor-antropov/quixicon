package com.quixicon.presentation.fragments.selectlanguagedialog.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quixicon.R
import com.quixicon.core.adapters.SpinnerAdapter
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.presentation.activities.start.models.LanguageModel
import timber.log.Timber

class SelectLanguageDialogFragment : DialogFragment() {

    private var selectedLanguage: LanguageCode? = null

    lateinit var languageAdapter: SpinnerAdapter<LanguageModel>

    var created = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        created = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.fragment_select_language_dialog, null)

        with(view) {
            // Subject language

            val spinner: Spinner = findViewById(R.id.spinner_language)

            val languagesSubject = resources.getStringArray(R.array.languages).toList().map { item ->
                val parts = item.split(":")
                val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
                LanguageModel(name = parts[1], value = defaultValue)
            }

            languageAdapter = SpinnerAdapter(requireContext(), R.layout.simple_spinner_item, languagesSubject).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = this
            }
            try {
                if (selectedLanguage != null) {
                    Timber.e("Set language to: $selectedLanguage")
                    val index = languagesSubject.indexOfFirst { it.value == selectedLanguage }
                    spinner.setSelection(index)
                } else {
                    Timber.e("No Language Selected")
                    val index = languagesSubject.indexOfFirst { it.value == LanguageCode.UNDEFINED }
                    spinner.setSelection(index)
                }
            } catch (e: Exception) {
                Timber.e("Set subject exception")
            }

            findViewById<View>(R.id.btn_ok).setOnClickListener {
                val item = (spinner.selectedItem as? LanguageModel)
                Timber.e("OK with language $item")
                (activity as? Listener)?.onLanguageSelect(item?.value)

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
            selectedLanguage = getSerializable(DEFAULT) as? LanguageCode
        }
    }

    /**
     * Restores state of fragment dialog.
     *
     * @param savedInstanceState Saved state of current fragment dialog.
     */
    protected fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also { restoreProperties(it) } ?: run {
            arguments?.also { restoreProperties(it) }
        }
    }

    /**
     * Saves current state of fragment dialog.
     *
     * @param outState Parameter for store current fragment dialog state.
     */
    protected fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putSerializable(DEFAULT, selectedLanguage)
        }
    }

    companion object {
        const val DEFAULT = "DEFAULT"

        @JvmStatic
        fun createInstance(
            defaultLanguage: LanguageCode?
        ) = SelectLanguageDialogFragment().apply {
            Timber.e("Language Dialog, default: $defaultLanguage")
            arguments = Bundle().apply {
                putSerializable(DEFAULT, defaultLanguage)
            }
        }
    }

    interface Listener {
        fun onLanguageSelect(language: LanguageCode?)
    }
}
