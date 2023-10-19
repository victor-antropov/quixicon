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
import com.quixicon.presentation.fragments.selectlanguagedialog.models.SelectLanguageModel
import timber.log.Timber

class SelectLanguageFilterDialogFragment : DialogFragment() {

    private var selectedSubjectLanguage: LanguageCode? = null
    private var selectedStudentLanguage: LanguageCode? = null

    lateinit var languageAdapter: SpinnerAdapter<SelectLanguageModel>

    var created = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        created = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.fragment_select_language_filter_dialog, null)

        with(view) {
            val spinnerSubject: Spinner = findViewById(R.id.spinner_language_subject)
            val spinnerStudent: Spinner = findViewById(R.id.spinner_language_student)

            val languagesSubject: MutableList<SelectLanguageModel> = resources.getStringArray(R.array.languages).toList().map { item ->
                val parts = item.split(":")
                val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
                SelectLanguageModel(name = parts[1], value = defaultValue)
            }.toMutableList()
            languagesSubject.add(0, SelectLanguageModel(getString(R.string.import_no_filter), null))

            languageAdapter = SpinnerAdapter(requireContext(), R.layout.simple_spinner_item, languagesSubject).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerSubject.adapter = this
            }
            try {
                if (selectedSubjectLanguage != null) {
                    val index = languagesSubject.indexOfFirst { it.value == selectedSubjectLanguage }
                    spinnerSubject.setSelection(index)
                } else {
                    Timber.e("No Language Selected")
                    val index = languagesSubject.indexOfFirst { it.value == null }
                    spinnerSubject.setSelection(index)
                }
            } catch (e: Exception) {
                Timber.e("Set subject exception")
            }

            // Student's language

            val studentCodes: List<String> = resources.getStringArray(R.array.lang_student_default_code).toList()

            val languages = resources.getStringArray(R.array.languages).toList().map { item ->
                val parts = item.split(":")
                val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
                SelectLanguageModel(name = parts[1], value = defaultValue)
            }.filter {
                studentCodes.contains(it.value?.value)
            }.toMutableList().apply {
                add(0, SelectLanguageModel(getString(R.string.lang_select_no), null))
            }

            SpinnerAdapter(requireContext(), R.layout.simple_spinner_item, languages).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerStudent.adapter = this
            }

            try {
                if (selectedStudentLanguage != null) {
                    val index = languages.indexOfFirst { it.value == selectedStudentLanguage }
                    spinnerStudent.setSelection(index)
                } else {
                    val index = languages.indexOfFirst { it.value == null }
                    spinnerStudent.setSelection(index)
                }
            } catch (e: Exception) {
                Timber.e("Set student exception")
            }

            // OK
            findViewById<View>(R.id.btn_ok).setOnClickListener {
                val itemSubject = (spinnerSubject.selectedItem as? SelectLanguageModel)
                val itemStudent = (spinnerStudent.selectedItem as? SelectLanguageModel)
                (activity as? Listener)?.onLanguageSelect(itemSubject?.value, itemStudent?.value)
                dismiss()
            }

            // Close
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
            selectedSubjectLanguage = getSerializable(SUBJECT_DEFAULT) as? LanguageCode
            selectedStudentLanguage = getSerializable(STUDENT_DEFAULT) as? LanguageCode
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
            putSerializable(SUBJECT_DEFAULT, selectedSubjectLanguage)
            putSerializable(STUDENT_DEFAULT, selectedStudentLanguage)
        }
    }

    companion object {
        const val SUBJECT_DEFAULT = "SUBJECT_DEFAULT"
        const val STUDENT_DEFAULT = "STUDENT_DEFAULT"

        @JvmStatic
        fun createInstance(
            defaultLanguageSubject: LanguageCode?,
            defaultLanguageStudent: LanguageCode? = null,
        ) = SelectLanguageFilterDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(SUBJECT_DEFAULT, defaultLanguageSubject)
                putSerializable(STUDENT_DEFAULT, defaultLanguageStudent)
            }
        }
    }

    interface Listener {
        fun onLanguageSelect(subject: LanguageCode?, student: LanguageCode?)
    }
}
