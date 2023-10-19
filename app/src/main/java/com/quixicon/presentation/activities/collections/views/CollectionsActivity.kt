package com.quixicon.presentation.activities.collections.views

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.quixicon.BR
import com.quixicon.R
import com.quixicon.background.helpers.AlarmHelper
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.collections.CollectionsEvent
import com.quixicon.core.metrics.entities.collections.NavEvent
import com.quixicon.core.metrics.entities.edit.EditCollectionEvent
import com.quixicon.core.metrics.entities.importcollection.ImportEvent
import com.quixicon.core.metrics.entities.preview.PreviewEvent
import com.quixicon.core.support.extensions.ActivityExtensions.isPermissionGranted
import com.quixicon.core.support.extensions.ActivityExtensions.requestPermission
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.showSnackbar
import com.quixicon.core.support.extensions.success
import com.quixicon.core.support.helpers.DisplayHelper
import com.quixicon.core.support.helpers.LanguageHelper
import com.quixicon.core.support.helpers.SupportHelper
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivityCollectionsBinding
import com.quixicon.databinding.NavHeaderBinding
import com.quixicon.databinding.RowCollectionBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.domain.entities.RequestCode
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.presentation.activities.cards.views.CardsActivity
import com.quixicon.presentation.activities.collectionpreview.views.CollectionPreviewActivity
import com.quixicon.presentation.activities.collections.adapters.CollectionsAdapter
import com.quixicon.presentation.activities.collections.models.CollectionModel
import com.quixicon.presentation.activities.collections.models.NavigationModel
import com.quixicon.presentation.activities.collections.viewmodels.CollectionsViewModel
import com.quixicon.presentation.activities.editcollection.views.EditCollectionActivity
import com.quixicon.presentation.activities.help.views.HelpActivity
import com.quixicon.presentation.activities.importcollection.views.ImportActivity
import com.quixicon.presentation.activities.settings.views.SettingsActivity
import com.quixicon.presentation.activities.social.views.SocialActivity
import com.quixicon.presentation.activities.test.views.TestActivity
import com.quixicon.presentation.fragments.selectdialog.views.AddCollectionDialogFragment
import com.quixicon.presentation.fragments.selectlanguagedialog.views.SelectLanguageDialogFragment
import com.quixicon.presentation.support.helpers.LanguageCodeHelper
import com.quixicon.presentation.views.AsyncOperationView
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.createBalloon
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CollectionsActivity :
    DaggerAppCompatActivity(),
    View.OnClickListener,
    AsyncOperationView,
    CollectionsAdapter.OnItemClickListener,
    NavigationView.OnNavigationItemSelectedListener,
    SelectLanguageDialogFragment.Listener,
    CollectionsView {

    private lateinit var binding: ActivityCollectionsBinding
    private lateinit var navHeaderBinding: NavHeaderBinding
    private lateinit var onDrawerListener: ActionBarDrawerToggle

    private lateinit var viewModel: CollectionsViewModel

    private lateinit var adapter: CollectionsAdapter

    private lateinit var config: QuixiconConfig

    private var tempFileName = "1.csv"

    private var tempCollectionModel: CollectionModel? = null

    private var tempCsvUri: Uri? = null

    private var balloons = mutableListOf<Balloon>()

    private var performDeepLink: Boolean = false

    private var isMultiLanguage: Boolean = false

    private var isTestButtonVisible: Boolean = true

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        Timber.e("Create Collections Activity: $savedInstanceState")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_collections)
        initViewModel()
        DisplayHelper.setDarkTheme(config.useDarkTheme)
        intent?.run {
            Timber.e("Action: $action")
            Timber.e("Type: $type")
            Timber.e("Data: $data")
            if (!performDeepLink && action == "android.intent.action.VIEW") {
                Timber.e("Start Handle deep link")
                if (data.toString().startsWith("quixicon")) {
                    parseExternalUrl(data.toString())
                } else {
                    handleDeepLink(this)
                }
            }
        }
        initComponents()
        AlarmHelper.init(this)
    }

    override fun onNewIntent(intent: Intent?) {
        Timber.e("NEW Intent")
        val globalAction = this@CollectionsActivity.intent.action
        intent?.run {
            Timber.e("Action: $action / $globalAction")
            Timber.e("Type: $type")
            Timber.e("Data: $data")
            if (action == "android.intent.action.VIEW") {
                if (data.toString().startsWith("quixicon")) {
                    parseExternalUrl(data.toString())
                } else {
                    handleDeepLink(this)
                }
            }
        }
        super.onNewIntent(intent)
    }

    override fun onResume() {
        config = viewModel.config

        if (viewModel.language != config.languageInterface.value) {
            LanguageHelper.setLocale(this, Locale(config.languageInterface.value))
            finish()
            startActivity(intent)
        } else if (LanguageHelper.synchronizeLocale(this, Locale(config.languageInterface.value))) {
            finish()
            startActivity(intent)
        }
        getCollections()

        val flagLanguageSubject = when {
            !isMultiLanguage -> LanguageHelper.getLanguageCode(getString(R.string.subject_code))
            config.useFilter -> config.languageSubject
            else -> null
        }

        val flagLanguageStudent = when {
            !isMultiLanguage -> config.languageStudent
            config.useFilter -> config.languageStudent
            else -> null
        }

        navHeaderBinding.model = NavigationModel(
            !config.useDarkTheme,
            subjectLanguage = flagLanguageSubject,
            studentLanguage = flagLanguageStudent
        )

        Glide.with(this).load(LanguageCodeHelper.getIcon(flagLanguageSubject)).into(binding.ivFlagEmpty)

        onDrawerListener.syncState()

        super.onResume()
    }

    override fun onPause() {
        viewModel.config = config
        super.onPause()
    }

    override fun onDestroy() {
        binding.drawerLayout.removeDrawerListener(onDrawerListener)
        Timber.e("Destroy Collections Activity")
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultData: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (requestCode == RequestCode.PICK_CSV.code) {
                resultData?.data?.also { uri ->
                    tempCsvUri = uri
                    // Один язык в приложениии
                    if (resources.getBoolean(R.bool.use_only_language)) {
                        val subject = LanguageHelper.getLanguageCode(getString(R.string.subject_code)) ?: LanguageCode.UNDEFINED
                        viewModel.importCollection(uri, getString(R.string.template_collection_name), subject)
                    }
                    // Много языков
                    else {
                        SelectLanguageDialogFragment.createInstance(
                            if (config.useFilter) config.languageSubject else null
                        ).apply {
                            isCancelable = false
                            val tag = SelectLanguageDialogFragment::class.java.simpleName
                            show(supportFragmentManager, tag)
                        }
                    }
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_theme_mode -> {
                Metrics.send(this, NavEvent.THEME)
                config.useDarkTheme = !config.useDarkTheme
                DisplayHelper.setDarkTheme(config.useDarkTheme)
            }
            R.id.btn_test -> {
                Metrics.send(this, CollectionsEvent.FAB_TEST)
                startTestActivity()
            }
            R.id.btn_add -> {
                Metrics.send(this, CollectionsEvent.FAB_ADD)
                AddCollectionDialogFragment.createInstance().apply { show(supportFragmentManager, tag) }
            }
            R.id.item_file_icon, R.id.item_file_text -> {
                Metrics.send(baseContext, ImportEvent.CSV_START)
                openFile()
            }
            R.id.item_file_help -> {
                callCsvHelpDialog()
            }
            R.id.item_new -> {
                Metrics.send(this, EditCollectionEvent.ADD_START)
                startActivity(Intent(this@CollectionsActivity, EditCollectionActivity::class.java))
            }
            R.id.item_server -> startActivity(Intent(this@CollectionsActivity, ImportActivity::class.java))
        }
    }

    override fun onAsyncOperationError(args: EventArgs<Throwable>) {}

    override fun onBindCollections(args: EventArgs<List<CollectionModel>>) {
        val collections = args.content
        Timber.e("BIND: ${collections.size}")
        if (collections.isNotEmpty()) {
            adapter.update(collections)
            binding.rvCollections.changeAppearance(true)
            collections.firstOrNull { it.size() > 0 && it.superType == null }?.run {
                binding.btnTest.show()
                isTestButtonVisible = true
                showHintsIntro()
            } ?: run {
                binding.btnTest.hide()
                isTestButtonVisible = false
            }
        } else {
            binding.rvCollections.changeAppearance(false)
            binding.btnTest.hide()
            isTestButtonVisible = false
        }
    }

    override fun onCollectionDelete(args: EventArgs<Unit>) {
        binding.root.showSnackbar(R.string.snack_delete_collection)
        getCollections()
    }

    override fun onGetExportData(args: EventArgs<ByteArray>) {
        val data = args.content
        saveCollectionToFile(tempFileName, data)
    }

    override fun onCollectionImported(args: EventArgs<Unit>) {
        Metrics.send(this, ImportEvent.CSV_FINISH)
        getCollections()
    }

    override fun onItemClick(variable: CollectionModel?, binding: RowCollectionBinding?) {
        val collection = variable!!
        Metrics.send(this, CollectionsEvent.SELECT)
        startActivity(
            Intent(this, CardsActivity::class.java).apply {
                putExtra(ExtraKey.COLLECTION.name, collection)
            }
        )
    }

    override fun onItemLongClick(variable: CollectionModel?, binding: RowCollectionBinding?) {
        if (variable == null || binding == null) return
        if (variable.superType != null) return
        val view = binding.ivMore

        Metrics.send(this, CollectionsEvent.CONTEXT_MENU)
        PopupMenu(this, view).apply {
            inflate(R.menu.menu_collection_context)
            if ((variable.collectionType != CollectionType.USER && variable.collectionType != CollectionType.EXAMPLE) || variable.size() == 0) {
                menu.findItem(R.id.item_export)?.apply {
                    isVisible = false
                }
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_edit -> {
                        Metrics.send(baseContext, EditCollectionEvent.EDIT_START)
                        startActivity(
                            Intent(this@CollectionsActivity, EditCollectionActivity::class.java).apply {
                                putExtra(ExtraKey.COLLECTION_ID.name, variable.id)
                            }
                        )
                    }
                    R.id.item_delete -> callDeleteDialog(variable)
                    R.id.item_export -> {
                        tempCollectionModel = variable
                        if (isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            callExportDialog(variable)
                        } else {
                            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, RequestCode.WRITE_PERMISSIONS)
                        }
                    }
                }
                true
            }
            show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestCode.WRITE_PERMISSIONS.code) {
            if (isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                tempCollectionModel?.run { callExportDialog(this) }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_review -> {
                Metrics.send(this, NavEvent.RATE)
                try {
                    val rateIntent = rateIntentForUrl("market://details")
                    startActivity(rateIntent)
                } catch (e: ActivityNotFoundException) {
                    val rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details")
                    startActivity(rateIntent)
                }
            }
            R.id.item_support -> {
                Metrics.send(this, NavEvent.SUPPORT)
                startActivity(SupportHelper.getTelegramIntent(this, getString(R.string.tg_chat)))
            }
            R.id.item_settings -> {
                Metrics.send(this, NavEvent.SETTINGS)
                startActivity(Intent(this@CollectionsActivity, SettingsActivity::class.java))
            }
            R.id.item_import -> {
                Metrics.send(this, NavEvent.IMPORT)
                startActivity(Intent(this@CollectionsActivity, ImportActivity::class.java))
            }
            R.id.item_help -> {
                Metrics.send(this, NavEvent.HELP)
                startActivity(Intent(this@CollectionsActivity, HelpActivity::class.java))
            }
            R.id.item_follow -> {
                Metrics.send(this, NavEvent.FOLLOW)
                startActivity(Intent(this@CollectionsActivity, SocialActivity::class.java))
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort_az -> {
                Metrics.send(this, CollectionsEvent.SORT_ABC)
                config.collectionSortOrder = CollectionSortOrder.AZ
                item.isChecked = true
                getCollections()
            }
            R.id.action_sort_date -> {
                Metrics.send(this, CollectionsEvent.SORT_RECENT)
                config.collectionSortOrder = CollectionSortOrder.RECENT
                item.isChecked = true
                getCollections()
            }

            R.id.action_add -> {
                Metrics.send(this, CollectionsEvent.TOP_ADD)
                AddCollectionDialogFragment.createInstance().apply { show(supportFragmentManager, tag) }
            }
            R.id.action_help -> {
                showHints()
                balloons.add(showBalloonCenter(binding.rvCollections, getString(R.string.hint_help)))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_collections, menu)

        val itemId = when (config.collectionSortOrder) {
            CollectionSortOrder.AZ -> R.id.action_sort_az
            CollectionSortOrder.RECENT -> R.id.action_sort_date
        }

        menu.findItem(itemId)?.apply {
            isChecked = true
        }

        return true
    }

    override fun onLanguageSelect(language: LanguageCode?) {
        Timber.e("Importing file with language: $language, ${tempCsvUri?.path}")
        tempCsvUri?.let {
            viewModel.importCollection(it, getString(R.string.template_collection_name), language)
        }
    }

    private fun initComponents() {
        setStatusBarColor(R.color.transparent)

        initToolbar(R.id.toolbar, R.string.app_title)

        with(binding) {
            executePendingBindings()
            invalidateAll()
        }

        isMultiLanguage = !resources.getBoolean(R.bool.use_only_language)

        onDrawerListener = object : ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            findViewById(R.id.toolbar),
            R.string.drawer_open,
            R.string.drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                Metrics.send(baseContext, CollectionsEvent.OPEN_NAV)
            }
        }

        binding.drawerLayout.addDrawerListener(onDrawerListener)
        binding.navView.setNavigationItemSelectedListener(this)
        binding.navView.menu.findItem(R.id.item_follow).isVisible = resources.getBoolean(R.bool.social_networks)

        binding.btnTest.setOnClickListener(this)
        binding.btnAdd.setOnClickListener(this)

        navHeaderBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0))
        navHeaderBinding.onClickListener = this
        navHeaderBinding.model = NavigationModel(
            !config.useDarkTheme,
            subjectLanguage = LanguageHelper.getLanguageCode(getString(R.string.subject_code)),
            studentLanguage = null
        )
        initAdapter()
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getCollectionsLiveData, ::onBindCollections)
            success(getCardDeletedLiveData, ::onCollectionDelete)
            success(getCollectionExportDataLiveData, ::onGetExportData)
            success(importCollectionLiveData, ::onCollectionImported)
        }
        config = viewModel.config
    }

    private fun initAdapter() {
        adapter = CollectionsAdapter(
            this,
            arrayListOf(),
            BR.collectionModel,
            R.layout.row_collection,
            BR.onClickListener,
            this,
            BR.onLongClickListener
        )

        binding.rvCollections.let {
            it.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            it.adapter = adapter
        }
    }

    private fun getCollections() {
        val filterSubject = if (config.useFilter) config.languageSubject else null
        // val filterStudent = if (config.useFilter && config.languageStudent != LanguageCode.UNDEFINED) config.languageStudent else null
        val filterStudent: LanguageCode? = null
        Timber.e("Get collections with filter: $filterSubject, $filterStudent")
        viewModel.getCollections(config.collectionSortOrder, filterSubject, filterStudent)
    }

    private fun callDeleteDialog(collection: CollectionModel) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_collection)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteCollection(collection.id)
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .create()
            .show()
    }

    private fun callExportDialog(collection: CollectionModel) {
        tempFileName = String.format(
            "%d_%s.csv",
            collection.id,
            SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        )

        val message = String.format(getString(R.string.dialog_export_message), collection.name, tempFileName)

        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(R.string.context_export)
            .setMessage(message)
            .setPositiveButton(R.string.Save) { _, _ ->
                viewModel.getExportData(collection.id)
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .create()
            .show()
    }

    private fun callCsvHelpDialog() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(R.string.action_help)
            .setMessage(R.string.import_file_help_message)
            .setPositiveButton(R.string.import_file_help_visit) { _, _ ->
                val url = getString(R.string.import_file_help_url)
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .create()
            .show()
    }

    private fun callDeepLinkDialog(id: String) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(R.string.preview_dialog_title)
            .setMessage(R.string.preview_dialog_message)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                startActivity(
                    Intent(this, CollectionPreviewActivity::class.java).apply {
                        putExtra(ExtraKey.COLLECTION_ID.name, id)
                    }
                )
                setDeepLinkPerformed()
            }
            .setNegativeButton(R.string.button_cancel) { _, _ ->
                Metrics.send(baseContext, PreviewEvent.CANCEL_DIALOG)
                setDeepLinkPerformed()
            }
            .create()
            .show()
    }

    private fun callSettingsHintDialog() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.main_settings_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.button_ok) { _, _ -> }
            .setNegativeButton(R.string.lang_error_open) { _, _ ->
                startActivity(Intent(this@CollectionsActivity, SettingsActivity::class.java))
            }
            .create()
            .show()
    }

    private fun callHelpHintDialog() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.main_help_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.button_ok) { _, _ -> }
            .setNegativeButton(R.string.open) { _, _ ->
                startActivity(Intent(this@CollectionsActivity, HelpActivity::class.java))
            }
            .create()
            .show()
    }

    private fun startTestActivity() {
        startActivity(Intent(this, TestActivity::class.java))
    }

    private fun saveCollectionToFile(fileName: String, data: ByteArray) {
        // Save file for Android 10+
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val details = ContentValues()
            details.put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            details.put(MediaStore.Downloads.IS_PENDING, 1)

            val downloadCollections =
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val contentUri = contentResolver.insert(downloadCollections, details)
            contentUri?.let {
                contentResolver.openFileDescriptor(it, "w").use { fileDescriptor ->
                    ParcelFileDescriptor.AutoCloseOutputStream(fileDescriptor).apply {
                        write(data)
                    }
                }
                details.clear()
                details.put(MediaStore.Downloads.IS_PENDING, 0)
                contentResolver.update(it, details, null, null)
                binding.root.showSnackbar(R.string.snack_collection_saved)
            }
        }
        // Android 9-
        else {
            val dir2 =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
            val file2 = File(dir2, fileName)
            try {
                FileOutputStream(file2).apply {
                    write(data)
                    close()
                }
                binding.root.showSnackbar(R.string.snack_collection_saved)
            } catch (e: Exception) {
                binding.root.showSnackbar(R.string.snack_file_error)
                Timber.e("Error ${e.message}")
            }
        }
    }

    private fun openFile() {
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime.getMimeTypeFromExtension("csv") // "text/comma-separated-values"
        }
        startActivityForResult(intent, RequestCode.PICK_CSV.code)
    }

    private fun rateIntentForUrl(url: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse(String.format("%s?id=%s", url, packageName))
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        }
    }

    private fun showHints() {
        if (isTestButtonVisible) balloons.add(showBalloon(binding.btnTest, getString(R.string.hint_test)))
        balloons.add(showBalloon(binding.btnAdd, getString(R.string.hint_collection)))
    }

    private fun showHintsIntro() {
        if (config.hintCollections) {
            showHints()
            if (isMultiLanguage) {
                callSettingsHintDialog()
            } else {
                callHelpHintDialog()
            }
        }
        config.hintCollections = false
    }

    private fun showBalloon(view: View, title: String): Balloon {
        val typedValue = TypedValue()

        theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
        val background = typedValue.data

        theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        val color = typedValue.data

        return createBalloon(this) {
            setPaddingLeft(12)
            setPaddingRight(12)
            setPaddingTop(12)
            setPaddingBottom(12)
            setCornerRadius(8f)
            setText(title)
            setTextSize(16f)
            setBalloonAnimation(BalloonAnimation.FADE)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowOrientation(ArrowOrientation.END)
            setArrowAlignAnchorPadding(-10)
            setLifecycleOwner(this@CollectionsActivity)
            setTextColor(color)
            setBackgroundColor(background)
            setDismissWhenClicked(false)
            setOnBalloonClickListener { dismissAllBallons() }
            setOnBalloonOutsideTouchListener { _, _ -> dismissAllBallons() }
        }.apply {
            showAlignStart(view)
        }
    }

    private fun showBalloonCenter(view: View, title: String): Balloon {
        val typedValue = TypedValue()

        theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
        val background = typedValue.data

        theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        val color = typedValue.data

        return createBalloon(this) {
            setPaddingLeft(12)
            setPaddingRight(12)
            setPaddingTop(12)
            setPaddingBottom(12)
            setCornerRadius(8f)
            setHeight(BalloonSizeSpec.WRAP)
            setWidthRatio(0.9f)
            setText(title)
            setTextSize(16f)
            setBalloonAnimation(BalloonAnimation.FADE)
            setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
            setArrowOrientation(ArrowOrientation.BOTTOM)
            setIsVisibleArrow(false)
            setLifecycleOwner(this@CollectionsActivity)
            setTextColor(color)
            setBackgroundColor(background)
            setDismissWhenClicked(false)
            setOnBalloonClickListener {
                dismissAllBallons()
                Metrics.send(this@CollectionsActivity, NavEvent.HELP)
                startActivity(Intent(this@CollectionsActivity, HelpActivity::class.java))
            }
            setOnBalloonOutsideTouchListener { _, _ -> dismissAllBallons() }
        }.apply {
            showAtCenter(view)
        }
    }

    private fun dismissAllBallons() {
        balloons.forEach {
            it.dismiss()
        }
    }

    private fun handleDeepLink(linkIntent: Intent) {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(linkIntent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                var deepLink: Uri? = null

                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                deepLink?.let { uri ->
                    val fullLink = uri.toString()
                    Timber.e("Full Link: $fullLink")

                    linkIntent.data?.getQueryParameter("id")?.let {
                        Metrics.send(baseContext, PreviewEvent.SHOW_DIALOG)
                        callDeepLinkDialog(it)
                    } ?: run {
                        parseExternalUrl(fullLink)
                    }
                }
            }.addOnFailureListener {
                Timber.e("handleIncomingDeepLinks: ${it.message}")
            }
    }

    private fun parseExternalUrl(url: String) {
        val parts = url.split("/")
        val i = parts.indexOf("collection")
        parts.getOrNull(i + 1)?.let {
            Metrics.send(baseContext, PreviewEvent.SHOW_DIALOG)
            callDeepLinkDialog(it)
        }
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putBoolean(DEEP_LINK_PERFORM, performDeepLink)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            performDeepLink = it.getBoolean(DEEP_LINK_PERFORM)
        } ?: run {
            intent?.also {
                performDeepLink = it.getBooleanExtra(DEEP_LINK_PERFORM, false)
            }
        }
    }

    fun setDeepLinkPerformed() {
        performDeepLink = true
        intent.putExtra(DEEP_LINK_PERFORM, true)
        intent.action = null
    }

    companion object {
        const val DEEP_LINK_PERFORM = "DEEP_LINK_PERFORM"
    }
}
