package com.quixicon.presentation.activities.editcollection.views

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.quixicon.R
import com.quixicon.core.adapters.SpinnerAdapter
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.edit.EditCollectionEvent
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.success
import com.quixicon.core.support.helpers.LanguageHelper
import com.quixicon.core.system.EventArgs
import com.quixicon.core.views.validatedtextview.extentions.validateInput
import com.quixicon.core.views.validatedtextview.functions.NotEmptyValidationFunction
import com.quixicon.databinding.ActivityEditCollectionBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.presentation.activities.editcollection.models.EditCollectionModel
import com.quixicon.presentation.activities.editcollection.viewmodels.EditCollectionViewModel
import com.quixicon.presentation.activities.start.models.LanguageModel
import com.quixicon.presentation.views.AsyncOperationView
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

class EditCollectionActivity : DaggerAppCompatActivity(), AsyncOperationView, AdapterView.OnItemSelectedListener, EditCollectionView {

    private lateinit var binding: ActivityEditCollectionBinding

    private lateinit var viewModel: EditCollectionViewModel

    private var collectionId: Long = 0

    var isMultiLanguage: Boolean = false

    private lateinit var config: QuixiconConfig

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    lateinit var languagesSubject: List<LanguageModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_collection)
        initComponents()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onAsyncOperationError(args: EventArgs<Throwable>) {
        val error = args.content
        Timber.e("Error: ${error.message}")
    }

    override fun onBindCollection(args: EventArgs<EditCollectionModel>) {
        val model = args.content
        model.isShowSubject = isMultiLanguage
        binding.model = model

        Timber.e("Set binding with $model")

        try {
            Timber.e("Set2 subject to: ${args.content.subject}")
            val index = languagesSubject.indexOfFirst { it.value == args.content.subject }
            binding.spinnerSubjectLanguage.setSelection(index)
        } catch (e: Exception) {
            Timber.e("Set2 subject exception")
        }
    }

    override fun onUpdateCollection(args: EventArgs<Unit>) {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        if (parent.id == R.id.spinner_subject_language) {
            val value = (parent.getItemAtPosition(position) as? LanguageModel)?.value
            value?.let {
                binding.model?.subject = it
                Timber.e("Subject Language Selected: $it")
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    fun initComponents() {
        initViewModel()

        isMultiLanguage = !resources.getBoolean(R.bool.use_only_language)

        val subject = if (isMultiLanguage) {
            config.languageSubject
        } else {
            LanguageHelper.getLanguageCode(getString(R.string.subject_code)) ?: LanguageCode.UNDEFINED
        }

        with(binding) {
            executePendingBindings()
            invalidateAll()
        }
        setStatusBarColor(R.color.transparent)

        binding.etName.validationFunction = NotEmptyValidationFunction()
        binding.btnSave.setOnClickListener { updateCollection() }

        if (collectionId > 0) {
            initToolbar(R.id.toolbar, R.string.rename_collection)
            getCollection(collectionId)
        } else {
            initToolbar(R.id.toolbar, R.string.add_collection)
            binding.model = EditCollectionModel(
                isShowSubjectDefault = isMultiLanguage,
                subject = subject
            )
        }

        // Subject

        // Subject language
        languagesSubject = resources.getStringArray(R.array.languages).toList().map { item ->
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
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getCollectionLiveData, ::onBindCollection)
            success(updateCollectionLiveData, ::onUpdateCollection)
        }
        config = viewModel.config
    }

    private fun getCollection(id: Long) {
        viewModel.getCollection(id)
    }

    private fun updateCollection() {
        if (validateInput(binding.container)) {
            if (collectionId > 0) {
                Metrics.send(this, EditCollectionEvent.EDIT_SAVE)
            } else {
                Metrics.send(this, EditCollectionEvent.ADD_SAVE)
            }
            binding.model?.let {
                Timber.e("Update Collection: ${it.subject}")
                viewModel.updateCollection(it)
            }
        }
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putLong(ExtraKey.COLLECTION_ID.name, collectionId)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            collectionId = it.getLong(ExtraKey.COLLECTION_ID.name)
        } ?: run {
            intent?.also {
                collectionId = it.getLongExtra(ExtraKey.COLLECTION_ID.name, 0)
            }
        }
    }
}
