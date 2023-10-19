package com.quixicon.presentation.activities.importcollection.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.quixicon.BR
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.importcollection.ImportEvent
import com.quixicon.core.metrics.entities.social.SocialEvent
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.showSnackbar
import com.quixicon.core.support.extensions.success
import com.quixicon.core.support.helpers.LanguageHelper
import com.quixicon.core.support.listeners.OnTabSelectedListener
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivityImportBinding
import com.quixicon.databinding.RowCollectionInfoBinding
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.CollectionPriceCategory
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.exceptions.Failure
import com.quixicon.domain.exceptions.FailureException
import com.quixicon.presentation.activities.base.DaggerInternetActivity
import com.quixicon.presentation.activities.importcollection.adapters.CollectionsInfoAdapter
import com.quixicon.presentation.activities.importcollection.models.CollectionInfoModel
import com.quixicon.presentation.activities.importcollection.viewmodels.ImportViewModel
import com.quixicon.presentation.activities.social.views.SocialActivity
import com.quixicon.presentation.fragments.selectlanguagedialog.views.SelectLanguageFilterDialogFragment
import com.quixicon.presentation.support.helpers.LanguageCodeHelper
import com.quixicon.presentation.views.AsyncOperationView
import timber.log.Timber
import javax.inject.Inject

class ImportActivity :
    DaggerInternetActivity(),
    AsyncOperationView,
    ImportView,
    OnTabSelectedListener,
    CollectionsInfoAdapter.OnItemClickListener,
    SelectLanguageFilterDialogFragment.Listener {

    private lateinit var binding: ActivityImportBinding

    private lateinit var viewModel: ImportViewModel

    private lateinit var adapter: CollectionsInfoAdapter

    private var tmpModel: CollectionInfoModel? = null

    private lateinit var config: QuixiconConfig

    private var isSocialAvailable: Boolean = true

    private var isMultiLanguages: Boolean = false

    private var subjectLanguage: LanguageCode? = null
    private var studentLanguage: LanguageCode? = null

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_import)
        initComponents()
        getRemoteCollections(CollectionPriceCategory.FREE)
        Metrics.send(this, ImportEvent.SERVER_CREATE)
    }

    override fun onSupportNavigateUp(): Boolean {
        Timber.e("Navigation Up")
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (isSocialAvailable && config.hintSocial) {
            callSocialDialog()
        } else {
            super.onBackPressed()
        }
    }

    override fun onAsyncOperationError(args: EventArgs<Throwable>) {
        val error = args.content
        Timber.e("Error: $error")

        val e = error as? FailureException ?: return

        when (val failure = e.failure) {
            is Failure.ServerError -> {
                Timber.e("Server Error: ${failure.code}")
                replaceServerErrorFragment(R.id.container)
            }
            Failure.ConnectionError,
            Failure.IoError -> {
                Timber.e("Connection error: $failure")
                replaceConnectionErrorFragment(R.id.container)
            }
        }
    }

    override fun onAsyncOperationProgress(args: EventArgs<Boolean>) {
        binding.progressBar.changeAppearance(args.content)
    }

    override fun invokeLatestRequest() {
        viewModel.invokeLatestRequest()
    }

    override fun onBindCollections(args: EventArgs<List<CollectionInfoModel>>) {
        val collections = args.content
        binding.rvCollections.changeAppearance(collections.isNotEmpty())
        adapter.update(collections)
    }

    override fun onCollectionImported(args: EventArgs<Int>) {
        tmpModel?.let {
            adapter.setInstalled(it, true)
            tmpModel = null
            binding.root.showSnackbar(R.string.import_snack_ok)
        }
    }

    override fun onCollectionDelete(args: EventArgs<Unit>) {
        tmpModel?.let {
            adapter.setInstalled(it, false)
            tmpModel = null
            binding.root.showSnackbar(R.string.snack_delete_collection)
        }
    }

    override fun onItemClick(variable: CollectionInfoModel?, binding: RowCollectionInfoBinding?) {
        variable?.link?.let {
            tmpModel = variable
            viewModel.importCollection(
                subject = LanguageHelper.getLanguageCode(getString(R.string.subject_code)) ?: LanguageCode.EN,
                student = config.languageStudent,
                url = it
            )
        }
        Metrics.send(baseContext, ImportEvent.SERVER_INSTALL)
    }

    override fun onItemLongClick(
        variable: CollectionInfoModel?,
        binding: RowCollectionInfoBinding?
    ) {
        if (variable == null || binding == null) return
        if (!variable.isInstalled) return
        val view = binding.ivCheck

        PopupMenu(this, view).apply {
            inflate(R.menu.menu_import_context)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_delete -> {
                        tmpModel = variable
                        deleteCollection(variable)
                    }
                }
                true
            }
            show()
        }

        super.onItemLongClick(variable, binding)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.position) {
            TAB_POSITION_FREE -> getRemoteCollections(CollectionPriceCategory.FREE)
            TAB_POSITION_PREMIUM -> {
                Metrics.send(baseContext, ImportEvent.SERVER_PREMIUM)
                getRemoteCollections(CollectionPriceCategory.PREMIUM)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_import, menu)
        menu.findItem(R.id.action_follow).isVisible = isSocialAvailable
        menu.findItem(R.id.action_filter).isVisible = isMultiLanguages
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_follow -> {
                Metrics.send(this, SocialEvent.IMPORT_MENU)
                startActivity(Intent(this, SocialActivity::class.java))
                config.hintSocial = false
                viewModel.config = config
            }
            R.id.action_filter -> {
                SelectLanguageFilterDialogFragment.createInstance(
                    subjectLanguage,
                    studentLanguage
                ).apply {
                    isCancelable = false
                    val tag = SelectLanguageFilterDialogFragment::class.java.simpleName
                    show(supportFragmentManager, tag)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLanguageSelect(subject: LanguageCode?, student: LanguageCode?) {
        val position = binding.tabLayout.selectedTabPosition
        Timber.e("You selected filter: $subject, $student, position: $position")
        subjectLanguage = subject
        studentLanguage = student
        Glide.with(this).load(LanguageCodeHelper.getIcon(subjectLanguage)).into(binding.ivFlagEmpty)
        getRemoteCollections(
            if (position == TAB_POSITION_FREE) {
                CollectionPriceCategory.FREE
            } else {
                CollectionPriceCategory.PREMIUM
            }
        )
    }

    private fun initComponents() {
        isSocialAvailable = resources.getBoolean(R.bool.social_networks)
        isMultiLanguages = !resources.getBoolean(R.bool.use_only_language)

        initViewModel()

        subjectLanguage = when {
            !isMultiLanguages -> LanguageHelper.getLanguageCode(getString(R.string.subject_code))
            config.useFilter -> config.languageSubject
            else -> null
        }

        studentLanguage = when {
            !isMultiLanguages -> config.languageStudent
            else -> null
            // !config.useFilter -> null
            // config.languageStudent == LanguageCode.UNDEFINED -> null
            // else -> config.languageStudent
        }

        with(binding) {
            executePendingBindings()
            invalidateAll()
        }
        setStatusBarColor(R.color.transparent)
        initToolbar(R.id.toolbar, R.string.title_activity_import)
        binding.tabLayout.addOnTabSelectedListener(this)
        initAdapter()

        binding.ivFlagEmpty.setOnClickListener {
            SelectLanguageFilterDialogFragment.createInstance(
                subjectLanguage
            ).apply {
                isCancelable = false
                val tag = SelectLanguageFilterDialogFragment::class.java.simpleName
                show(supportFragmentManager, tag)
            }
        }

        Glide.with(this).load(LanguageCodeHelper.getIcon(subjectLanguage)).into(binding.ivFlagEmpty)
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getCollectionsLiveData, ::onBindCollections)
            success(importCollectionLiveData, ::onCollectionImported)
            success(getCollectionDeletedLiveData, ::onCollectionDelete)
        }
        config = viewModel.config
    }

    private fun initAdapter() {
        adapter = CollectionsInfoAdapter(
            this,
            arrayListOf(),
            BR.collectionModel,
            R.layout.row_collection_info,
            BR.onClickListener,
            this,
            BR.onLongClickListener
        )

        binding.rvCollections.let {
            it.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            it.adapter = adapter
        }
    }

    private fun getRemoteCollections(category: CollectionPriceCategory) {
        viewModel.getRemoteCollections(
            filterSubject = subjectLanguage,
            filterStudent = studentLanguage,
            category = category
        )
    }

    private fun callSocialDialog() {
        config.hintSocial = false
        viewModel.config = config

        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setCancelable(true)
            .setTitle(R.string.menu_follow)
            .setMessage(R.string.social_message)
            .setPositiveButton(R.string.social_yes) { _, _ ->
                Metrics.send(this, SocialEvent.IMPORT_MENU)
                startActivity(Intent(this, SocialActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.social_no) { _, _ ->
                finish()
            }
            .create()
            .show()
    }

    private fun deleteCollection(collection: CollectionInfoModel) {
        if (collection.isInstalled) viewModel.deleteCollection(collection.id)
    }

    companion object {
        val TAB_POSITION_FREE = 0
        val TAB_POSITION_PREMIUM = 1
    }
}
