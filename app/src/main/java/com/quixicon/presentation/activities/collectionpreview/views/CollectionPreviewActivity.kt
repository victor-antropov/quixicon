package com.quixicon.presentation.activities.collectionpreview.views

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quixicon.BR
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.preview.PreviewEvent
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.replaceFragment
import com.quixicon.core.support.extensions.showSnackbar
import com.quixicon.core.support.extensions.success
import com.quixicon.core.support.helpers.LanguageHelper
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivityCollectionPreviewBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.exceptions.Failure
import com.quixicon.domain.exceptions.FailureException
import com.quixicon.presentation.activities.base.DaggerInternetActivity
import com.quixicon.presentation.activities.cards.views.CardsActivity
import com.quixicon.presentation.activities.collectionpreview.adapters.CardsPreviewAdapter
import com.quixicon.presentation.activities.collectionpreview.models.CollectionPreviewModel
import com.quixicon.presentation.activities.collectionpreview.viewmodels.CollectionPreviewViewModel
import com.quixicon.presentation.activities.collections.models.CollectionModel
import com.quixicon.presentation.fragments.errors.views.CollectionPreviewErrorFragment
import com.quixicon.presentation.views.AsyncOperationView
import timber.log.Timber
import javax.inject.Inject
import kotlin.NoSuchElementException

class CollectionPreviewActivity : DaggerInternetActivity(), AsyncOperationView, CollectionPreviewView {

    private lateinit var binding: ActivityCollectionPreviewBinding

    private lateinit var viewModel: CollectionPreviewViewModel

    private lateinit var adapter: CardsPreviewAdapter

    private lateinit var config: QuixiconConfig

    private var remoteCollectionId: String? = null

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        Timber.e("Create Activity: ${intent.extras}")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_collection_preview)
        initViewModel()
        initComponents()
        getCollectionPreview(remoteCollectionId)
    }

    override fun onDestroy() {
        Timber.e("Destroy Activity")
        super.onDestroy()
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

        if (error is NoSuchElementException) {
            replaceCollectionPreviewErrorFragment()
        } else if (error is FailureException) {
            when (val failure = error.failure) {
                is Failure.ServerError -> {
                    Timber.e("Server Error: ${failure.code}")
                    replaceServerErrorFragment(R.id.container)
                }
                Failure.ConnectionError,
                Failure.IoError,
                -> {
                    Timber.e("Connection error: $failure")
                    replaceConnectionErrorFragment(R.id.container)
                }
            }
        }
    }

    override fun onAsyncOperationProgress(args: EventArgs<Boolean>) {
        binding.progressBar.changeAppearance(args.content)
    }

    override fun invokeLatestRequest() {
        viewModel.invokeLatestRequest()
    }

    override fun onBindCollection(args: EventArgs<CollectionPreviewModel>) {
        val collection = args.content
        binding.collectionModel = collection
        adapter.update(collection.cards)

        if (collection.isInstalled()) {
            Metrics.send(baseContext, PreviewEvent.OLD_COLLECTION)
        } else {
            Metrics.send(baseContext, PreviewEvent.NEW_COLLECTION)
        }
    }

    override fun onCollectionImported(args: EventArgs<Int>) {
        Timber.e(getString(R.string.import_snack_ok))
        binding.root.showSnackbar(R.string.import_snack_ok)
        finish()
    }

    private fun initComponents() {
        setStatusBarColor(R.color.transparent)
        initToolbar(R.id.toolbar, "")

        adapter = CardsPreviewAdapter(
            this,
            arrayListOf(),
            BR.cardModel,
            R.layout.row_card_preview,
        )

        binding.collectionModel = CollectionPreviewModel("")

        binding.rvCards.let {
            it.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            it.adapter = adapter
        }

        binding.btnInstall.setOnClickListener {
            Metrics.send(baseContext, PreviewEvent.INSTALL)
            viewModel.importThisCollection()
        }

        binding.btnForward.setOnClickListener {
            finish()
            startCardsActivity()
        }
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getRemoteCollectionLiveData, ::onBindCollection)
            success(importCollectionLiveData, ::onCollectionImported)
        }
        config = viewModel.config
    }

    private fun getCollectionPreview(id: String?) {
        id?.let {
            binding.progressBar.changeAppearance(true)
            viewModel.getRemoteCollection(
                subject = LanguageHelper.getLanguageCode(getString(R.string.subject_code)) ?: LanguageCode.EN,
                student = config.languageStudent,
                id = it,
            )
        }
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putString(ExtraKey.COLLECTION_ID.name, remoteCollectionId)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            remoteCollectionId = it.getString(ExtraKey.COLLECTION_ID.name)
        } ?: run {
            intent?.also {
                remoteCollectionId = it.getStringExtra(ExtraKey.COLLECTION_ID.name)
            }
        }
    }

    private fun startCardsActivity() {
        binding.collectionModel?.let {
            val collection = CollectionModel(
                id = it.installedCollectionId,
                name = it.name,
                description = it.description,
                qntWords = it.qntWords,
                qntPhrases = it.qntPhrases,
                qntRules = it.qntRules,
            )

            startActivity(
                Intent(this, CardsActivity::class.java).apply {
                    putExtra(ExtraKey.COLLECTION.name, collection)
                },
            )
        }
    }

    private fun replaceCollectionPreviewErrorFragment() {
        replaceFragment(R.id.container, CollectionPreviewErrorFragment.createInstance(), false)
    }
}
