package com.quixicon.presentation.activities.settings.views

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quixicon.BuildConfig
import com.quixicon.R
import com.quixicon.background.helpers.WorkerHelper
import com.quixicon.core.adapters.SpinnerAdapter
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.notification.NotificationEvent
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.success
import com.quixicon.core.support.helpers.DisplayHelper
import com.quixicon.core.support.helpers.LanguageHelper
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivitySettingsBinding
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.enums.NotificationSource
import com.quixicon.presentation.activities.settings.models.NotificationSourceModel
import com.quixicon.presentation.activities.settings.models.SettingsModel
import com.quixicon.presentation.activities.settings.viewmodels.SettingsViewModel
import com.quixicon.presentation.activities.start.models.LanguageModel
import com.quixicon.presentation.activities.start.models.LanguageSelectModel
import com.quixicon.presentation.activities.test.models.TestCollectionModel
import com.quixicon.presentation.views.AsyncOperationView
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SettingsActivity : DaggerAppCompatActivity(), AsyncOperationView, AdapterView.OnItemSelectedListener, SettingsView {

    private lateinit var binding: ActivitySettingsBinding

    private lateinit var viewModel: SettingsViewModel

    private lateinit var config: QuixiconConfig

    private var hasActiveDialog = false

    private var isMultiLanguage: Boolean = false

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        Timber.e("Create Settings Activity")
        initComponents()
        getCollections()
    }

    override fun onDestroy() {
        Timber.e("Destroy Settings Activity")
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (config == viewModel.config) {
            super.onBackPressed()
        } else {
            callFinishingDialog()
        }
    }

    override fun onAsyncOperationError(args: EventArgs<Throwable>) {
        val error = args.content
        Timber.e("Error: ${error.message}")
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        if (parent.id == R.id.spinner_language_interface) {
            val value = (parent.getItemAtPosition(position) as? LanguageModel)?.value
            value?.let {
                if (config.languageInterface != it) {
                    config.languageInterface = it
                    LanguageHelper.setLocale(this, Locale(it.value))
                    viewModel.config = config
                    finish()
                    startActivity(intent)
                }
            }
        }

        if (parent.id == R.id.spinner_notifications_source) {
            val value = (parent.getItemAtPosition(position) as? NotificationSourceModel)?.value
            value?.let {
                config.notificationSource = it
                binding.settingsModel?.isSourceFixed = it == NotificationSource.FIXED
            }
        }

        if (parent.id == R.id.spinner_collections) {
            val value = (parent.getItemAtPosition(position) as? TestCollectionModel)?.id
            value?.let {
                config.notificationTestCollectionId = it
            }
        }

        if (parent.id == R.id.spinner_subject_language) {
            val value = (parent.getItemAtPosition(position) as? LanguageModel)?.value
            value?.let {
                if (config.languageSubject != it) {
                    config.languageSubject = it
                }
            }
        }

        if (parent.id == R.id.spinner_student_language) {
            val value = (parent.getItemAtPosition(position) as? LanguageSelectModel)?.value

            Timber.e("You selected Student: $value")
            value?.let {
                if (config.languageStudent != it) {
                    config.languageStudent = it
                }
            } ?: run {
                config.languageStudent = LanguageCode.UNDEFINED
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onBindCollections(args: EventArgs<List<TestCollectionModel>>) {
        val collections = args.content

        // collection
        SpinnerAdapter(this, R.layout.simple_spinner_item, collections).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCollections.adapter = this
        }

        try {
            val index = collections.indexOfFirst { it.id == config.notificationTestCollectionId }
            binding.spinnerCollections.setSelection(index)
        } catch (e: Exception) {}

        binding.spinnerCollections.onItemSelectedListener = this
    }

    fun initComponents() {
        initViewModel()

        isMultiLanguage = !resources.getBoolean(R.bool.use_only_language)

        with(binding) {
            executePendingBindings()
            invalidateAll()
        }
        setStatusBarColor(R.color.transparent)

        initToolbar(R.id.toolbar, R.string.title_activity_settings)

        binding.settingsModel = SettingsModel(
            config.useDarkTheme,
            config.useNotifications,
            BuildConfig.VERSION_NAME,
            config.isDrawAnswers,
            config.showGlobalCollections,
            config.useFilter,
            isMultiLanguage
        ).apply {
            isSourceFixed = config.notificationSource == NotificationSource.FIXED
        }

        // Interface language
        val languagesInterface = resources.getStringArray(R.array.lang_interface).toList().map { item ->
            val parts = item.split(":")
            val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
            LanguageModel(name = parts[1], value = defaultValue)
        }

        SpinnerAdapter(this, R.layout.simple_spinner_item, languagesInterface).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerLanguageInterface.adapter = this
        }
        try {
            val index = languagesInterface.indexOfFirst { it.value == config.languageInterface }
            binding.spinnerLanguageInterface.setSelection(index)
        } catch (e: Exception) {}

        binding.spinnerLanguageInterface.onItemSelectedListener = this

        with(binding.switchDark) {
            setOnClickListener {
                val b: Boolean = binding.settingsModel?.isDarkMode ?: false
                if (b != config.useDarkTheme) {
                    config.useDarkTheme = b
                }
            }
        }

        with(binding.switchDraw) {
            setOnClickListener {
                val b: Boolean = binding.settingsModel?.isDrawOn ?: false
                if (b != config.isDrawAnswers) {
                    config.isDrawAnswers = b
                }
            }
        }

        with(binding.switchGlobal) {
            setOnClickListener {
                val b: Boolean = binding.settingsModel?.isShowGlobal ?: false
                if (b != config.showGlobalCollections) {
                    config.showGlobalCollections = b
                }
            }
        }

        // Notifications
        with(binding.switchNotifications) {
            setOnClickListener {
                val b = binding.settingsModel?.useNotifications
                config.useNotifications = b!!
                if (config.useNotifications) {
                    Metrics.send(baseContext, NotificationEvent.ON)
                } else {
                    Metrics.send(baseContext, NotificationEvent.OFF)
                }
            }
        }

        // Filter
        with(binding.switchFilterSubject) {
            setOnClickListener {
                val b = binding.settingsModel?.useFilter
                config.useFilter = b!!
            }
        }

        // Subject language
        val languagesSubject = resources.getStringArray(R.array.languages).toList().map { item ->
            val parts = item.split(":")
            val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
            LanguageModel(name = parts[1], value = defaultValue)
        }

        SpinnerAdapter(this, R.layout.simple_spinner_item, languagesSubject).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubjectLanguage.adapter = this
        }
        try {
            Timber.e("Set subject to: ${config.languageSubject}")
            val index = languagesSubject.indexOfFirst { it.value == config.languageSubject }
            binding.spinnerSubjectLanguage.setSelection(index)
        } catch (e: Exception) {
            Timber.e("Set subject exception")
        }

        binding.spinnerSubjectLanguage.onItemSelectedListener = this

        // Student's Language

        val studentCodes: List<String> = resources.getStringArray(R.array.lang_student_default_code).toList()

        val languages = resources.getStringArray(R.array.languages).toList().map { item ->
            val parts = item.split(":")
            val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
            LanguageSelectModel(name = parts[1], value = defaultValue)
        }.filter {
            studentCodes.contains(it.value?.value)
        }.toMutableList().apply {
            add(0, LanguageSelectModel(getString(R.string.lang_select_no), null))
        }

        SpinnerAdapter(this, R.layout.simple_spinner_item, languages).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerStudentLanguage.adapter = this
        }

        try {
            val currentLanguage = config.languageStudent
            val index = languages.indexOfFirst { it.value == currentLanguage }
            Timber.e("Set student to $currentLanguage, index $index")
            binding.spinnerStudentLanguage.setSelection(index)
        } catch (e: Exception) {
            Timber.e("Set student exception")
        }

        binding.spinnerStudentLanguage.onItemSelectedListener = this

        // source
        val sources = listOf(
            NotificationSourceModel(getString(R.string.settings_source_recent), NotificationSource.RECENT),
            NotificationSourceModel(getString(R.string.settings_source_fixed), NotificationSource.FIXED)
        )

        SpinnerAdapter(this, R.layout.simple_spinner_item, sources).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerNotificationsSource.adapter = this
        }

        try {
            val index = sources.indexOfFirst { it.value == config.notificationSource }
            binding.spinnerNotificationsSource.setSelection(index)
        } catch (e: Exception) {}

        binding.spinnerNotificationsSource.onItemSelectedListener = this

        // start test notification
        binding.btnStart.setOnClickListener {
            viewModel.config = config
            WorkerHelper.runGetFlahcardWorker(this, 0, false)
        }
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getCollectionsLiveData, ::onBindCollections)
        }
        config = viewModel.config
    }

    private fun callFinishingDialog() {
        if (!hasActiveDialog) {
            hasActiveDialog = true
            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
                .setCancelable(true)
                .setTitle(R.string.Save)
                .setMessage(R.string.dialog_settings_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    DisplayHelper.setDarkTheme(config.useDarkTheme)
                    viewModel.config = config
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .setOnDismissListener {
                    hasActiveDialog = false
                    finish()
                }
                .create()
                .show()
        }
    }

    private fun getCollections() {
        viewModel.getCollections()
    }
}
