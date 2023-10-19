package com.quixicon.presentation.activities.editcard.views

import android.app.Activity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.edit.EditCardEvent
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.success
import com.quixicon.core.system.EventArgs
import com.quixicon.core.views.validatedtextview.extentions.validateInput
import com.quixicon.core.views.validatedtextview.functions.NotEmptyValidationFunction
import com.quixicon.databinding.ActivityEditCardBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.presentation.activities.editcard.models.EditCardModel
import com.quixicon.presentation.activities.editcard.viewmodels.EditCardViewModel
import com.quixicon.presentation.views.AsyncOperationView
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

class EditCardActivity : DaggerAppCompatActivity(), AsyncOperationView, EditCardView {

    private lateinit var binding: ActivityEditCardBinding

    private lateinit var viewModel: EditCardViewModel

    private var cardId: Long = 0

    private var collectionId: Long = 0

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_card)
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

    override fun onBindCard(args: EventArgs<EditCardModel>) {
        binding.cardModel = args.content
    }

    override fun onUpdateCard(args: EventArgs<Unit>) {
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun initComponents() {
        initViewModel()

        with(binding) {
            executePendingBindings()
            invalidateAll()
        }
        setStatusBarColor(R.color.transparent)

        if (cardId > 0) {
            Metrics.send(this, EditCardEvent.EDIT_START)
            initToolbar(R.id.toolbar, R.string.edit_card)
            getCard(cardId)
        } else {
            Metrics.send(this, EditCardEvent.ADD_SAVE)
            initToolbar(R.id.toolbar, R.string.add_card)
            binding.cardModel = EditCardModel()
        }

        binding.etOriginal.validationFunction = NotEmptyValidationFunction()
        binding.btnSave.setOnClickListener { updateCard() }
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getCardLiveData, ::onBindCard)
            success(updateCardLiveData, ::onUpdateCard)
        }
    }

    private fun getCard(id: Long) {
        viewModel.getCard(id)
    }

    private fun updateCard() {
        if (validateInput(binding.container)) {
            if (cardId > 0) {
                Metrics.send(this, EditCardEvent.EDIT_SAVE)
            } else {
                Metrics.send(this, EditCardEvent.ADD_SAVE)
            }
            binding.cardModel?.let { viewModel.updateCard(it, collectionId) }
        }
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putLong(ExtraKey.CARD_ID.name, cardId)
            putLong(ExtraKey.COLLECTION_ID.name, collectionId)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            cardId = it.getLong(ExtraKey.CARD_ID.name)
            collectionId = it.getLong(ExtraKey.COLLECTION_ID.name)
        } ?: run {
            intent?.also {
                cardId = it.getLongExtra(ExtraKey.CARD_ID.name, 0)
                collectionId = it.getLongExtra(ExtraKey.COLLECTION_ID.name, 0)
            }
        }
    }
}
