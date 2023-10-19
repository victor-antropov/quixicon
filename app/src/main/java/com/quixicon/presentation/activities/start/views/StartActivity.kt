package com.quixicon.presentation.activities.start.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.quixicon.R
import com.quixicon.core.adapters.SpinnerAdapter
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.collections.CollectionsEvent
import com.quixicon.core.metrics.entities.install.InstallStartEvent
import com.quixicon.core.support.extensions.*
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.helpers.DisplayHelper
import com.quixicon.core.support.helpers.LanguageHelper
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivityStartBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.presentation.activities.collections.views.CollectionsActivity
import com.quixicon.presentation.activities.start.models.LanguageSelectModel
import com.quixicon.presentation.activities.start.viewmodels.StartViewModel
import com.quixicon.presentation.activities.test.views.TestActivity
import com.quixicon.presentation.views.AsyncOperationView
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

class StartActivity : DaggerAppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener, AsyncOperationView, StartView {

    private lateinit var binding: ActivityStartBinding

    private lateinit var viewModel: StartViewModel

    private lateinit var config: QuixiconConfig

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    var isMultiLanguages: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start)
        setStatusBarColor(R.color.transparent)

        initViewModel()
        Timber.e("Config: $config")
        Metrics.setUseNotificationTest(config.useNotifications)

        DisplayHelper.setDarkTheme(config.useDarkTheme)

        isMultiLanguages = !resources.getBoolean(R.bool.use_only_language)

        if (config.isInstalled) {
            Timber.e("skip start activity")
            startMainActivity()
        } else {
            initComponents()
            Metrics.send(this, InstallStartEvent.CREATE)
        }
    }

    override fun onClick(view: View) {
        Timber.e("OnClick!")
        if (view.id == R.id.btn_next) {
            Metrics.send(this, InstallStartEvent.START)
            createDb()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val item = parent.getItemAtPosition(position)

        if (parent.id == R.id.spinner_language_subject) {
            val model = item as? LanguageSelectModel
            Timber.e("You selected subject: $model")
            initStudentList(model?.value)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onAsyncOperationError(args: EventArgs<Throwable>) {
        val error = args.content
        Timber.e("Error: ${error.message}")
        error.printStackTrace()
        config.isInstalled = true
        config.useDarkTheme = true
        config.isDrawAnswers = resources.getBoolean(R.bool.draw_available)
        viewModel.config = config
        startMainActivity()
    }

    override fun onAsyncOperationProgress(args: EventArgs<Boolean>) {
        if (args.content) {
            binding.progressBar.changeAppearance(true)
            binding.infoLayout.changeAppearance(false)
        }
    }

    override fun onMultiCollectionsImportedSuccess(args: EventArgs<Int>) {
        val result = args.content
        Timber.e("We've just imported $result collections.")
        Metrics.send(this, InstallStartEvent.FINISH)
        config.isInstalled = true
        config.useDarkTheme = true
        config.isDrawAnswers = resources.getBoolean(R.bool.draw_available)
        config.useNotifications = resources.getBoolean(R.bool.enable_notifications)

        viewModel.config = config

        if (resources.getBoolean(R.bool.start_intro)) {
            startTestActivity()
        } else {
            startMainActivity()
        }
    }

    private fun initComponents() {
        with(binding) {
            executePendingBindings()
            invalidateAll()
            btnNext.setOnClickListener(this@StartActivity)
            infoLayout.changeAppearance(true)
            progressBar.changeAppearance(false)
            binding.layoutSubject.changeAppearance(isMultiLanguages)
        }

        // Subject's Language
        if (isMultiLanguages) {
            binding.spinnerLanguageSubject.onItemSelectedListener = this

            val languagesSubject = resources.getStringArray(R.array.languages).toList().map { item ->
                val parts = item.split(":")
                val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
                LanguageSelectModel(name = parts[1], value = defaultValue)
            }.toMutableList()
            languagesSubject.add(0, LanguageSelectModel(getString(R.string.lang_select_later), null))
            SpinnerAdapter(this, R.layout.simple_spinner_item, languagesSubject).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerLanguageSubject.adapter = this
            }

            try {
                val currentLanguage = LanguageHelper.getCurrentLanguage(this)
                val defaultLanguage: LanguageCode? = if (currentLanguage == LanguageCode.RU) LanguageCode.EN else null
                val index = languagesSubject.indexOfFirst { it.value == defaultLanguage }
                binding.spinnerLanguageSubject.setSelection(index)
            } catch (e: Exception) {}
        } else {
            initStudentList(LanguageHelper.getLanguageCode(getString(R.string.subject_code)))
        }

        // Interface language
        val languagesInterface = resources.getStringArray(R.array.lang_interface).toList().map { item ->
            val parts = item.split(":")
            val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
            LanguageSelectModel(name = parts[1], value = defaultValue)
        }
        val currentLanguage = LanguageHelper.getCurrentLanguage(this)
        config.languageInterface = languagesInterface.firstOrNull { it.value == currentLanguage }?.value ?: LanguageCode.EN
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(importMultiCollectionsLiveData, ::onMultiCollectionsImportedSuccess)
        }
        config = viewModel.config
    }

    private fun createDb() {
        val arrayId: Int
        val selectedSubject: LanguageCode?
        val selectedStudent: LanguageCode?

        config.languageStudent = (binding.spinnerLanguageStudent.selectedItem as? LanguageSelectModel)?.value ?: LanguageCode.EN

        if (isMultiLanguages) {
            val subject = (binding.spinnerLanguageSubject.selectedItem as? LanguageSelectModel)?.value

            if (subject == null) {
                config.useFilter = false
                config.languageSubject = LanguageCode.UNDEFINED
                arrayId = R.array.init_collections_multi
                selectedStudent = null
            } else {
                config.useFilter = true
                config.languageSubject = subject
                arrayId = R.array.init_collections
                selectedStudent = config.languageStudent
            }
            selectedSubject = subject
        } else {
            config.useFilter = true
            selectedSubject = LanguageHelper.getLanguageCode(getString(R.string.subject_code)) ?: LanguageCode.UNDEFINED
            selectedStudent = config.languageStudent
            config.languageSubject = selectedSubject
            arrayId = R.array.init_collections
        }

        Timber.e("Create DB $selectedSubject, $selectedStudent")

        val collections = resources.getStringArray(arrayId)

        viewModel.importMultiCollections(
            collections.toCollection(ArrayList()),
            subjectLanguage = selectedSubject,
            studentLanguage = selectedStudent
        )
    }

    private fun startMainActivity() {
        Metrics.send(this, CollectionsEvent.CREATE)
        startActivity(Intent(this, CollectionsActivity::class.java))
        finishAffinity()
    }

    private fun startTestActivity() {
        startActivity(
            Intent(this, TestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(ExtraKey.IS_INTRO.name, true)
            }
        )
        finishAffinity()
    }

    private fun initStudentList(code: LanguageCode?) {
        Timber.e("Init student: $code")

        val resId: Int = when (code) {
            LanguageCode.EN -> R.array.lang_student_en_code
            LanguageCode.ES -> R.array.lang_student_es_code
            LanguageCode.FR -> R.array.lang_student_fr_code
            LanguageCode.SV -> R.array.lang_student_sv_code
            LanguageCode.HI -> R.array.lang_student_hi_code
            else -> R.array.lang_student_default_code
        }

        val studentCodes: List<String> = resources.getStringArray(resId).toList()

        val languages = resources.getStringArray(R.array.languages).toList().map { item ->
            val parts = item.split(":")
            val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
            LanguageSelectModel(name = parts[1], value = defaultValue)
        }.filter {
            studentCodes.contains(it.value?.value)
        }

        SpinnerAdapter(this, R.layout.simple_spinner_item, languages).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerLanguageStudent.adapter = this
        }

        try {
            val currentLanguage = LanguageHelper.getCurrentLanguage(this)
            val index = languages.indexOfFirst { it.value == currentLanguage }
            binding.spinnerLanguageStudent.setSelection(index)
        } catch (e: Exception) {}
    }
}
