package com.quixicon.presentation.activities.test.views

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.book.BookEvent
import com.quixicon.core.metrics.entities.install.InstallTestEvent
import com.quixicon.core.metrics.entities.test.TestEvent
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
import com.quixicon.databinding.ActivityTestBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.enums.TestDirection
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.models.CardModel
import com.quixicon.presentation.activities.cards.viewmodels.SpeechViewModel
import com.quixicon.presentation.activities.collections.models.CollectionModel
import com.quixicon.presentation.activities.collections.views.CollectionsActivity
import com.quixicon.presentation.activities.start.models.LanguageModel
import com.quixicon.presentation.activities.test.models.TestCollectionModel
import com.quixicon.presentation.activities.test.viewmodels.TestViewModel
import com.quixicon.presentation.fragments.cardaction.views.CardActionDialogFragment
import com.quixicon.presentation.fragments.draw.models.DrawModel
import com.quixicon.presentation.fragments.draw.views.DrawDialogFragment
import com.quixicon.presentation.fragments.infodialog.views.InfoDialogFragment
import com.quixicon.presentation.fragments.test.listeners.TestSettingsListener
import com.quixicon.presentation.fragments.test.models.TestProcessModel
import com.quixicon.presentation.fragments.test.models.TestSettingsModel
import com.quixicon.presentation.fragments.test.views.TestProcessFragment
import com.quixicon.presentation.fragments.test.views.TestResultFragment
import com.quixicon.presentation.fragments.test.views.TestSettingsFragment
import com.quixicon.presentation.fragments.test.views.TestSettingsIntroFragment
import com.quixicon.presentation.views.AsyncOperationView
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TestActivity :
    DaggerAppCompatActivity(),
    AsyncOperationView,
    TestView,
    TestSettingsListener,
    TestProcessFragment.Listener,
    TestResultFragment.Listener,
    CardActionDialogFragment.Listener,
    DrawDialogFragment.Listener {

    private lateinit var binding: ActivityTestBinding

    private lateinit var viewModel: TestViewModel

    private var collection: CollectionModel? = null

    private var isIntro: Boolean = false

    private var cards: List<CardModel>? = null

    private lateinit var testSettingsModel: TestSettingsModel

    private var dialogFragment: CardActionDialogFragment? = null

    private var drawDialogFragment: DrawDialogFragment? = null

    private var drawRestore: Boolean = false

    private var topMenu: Menu? = null

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var config: QuixiconConfig

    private val speechViewModel: SpeechViewModel by viewModels()

    var subjectModel: LanguageModel? = null

    private var isTTSAvailable = false

    private var ttsStatus: Int = TTS_STATUS_OK

    private var textToSpeechEngine: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.e("Create Test Activity: $savedInstanceState")
        super.onCreate(null) // Чтобы предотвратить восстановление фрагметов
        restoreInstanceState(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test)
        initComponents()
        getCollections()

        if (isIntro) Metrics.send(this, InstallTestEvent.CREATE)
    }

    override fun onPause() {
        viewModel.config = config
        super.onPause()
    }

    override fun onDestroy() {
        Timber.e("Destroy Test Activity")
        speechViewModel.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_test, menu)
        topMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveInstanceState(outState)
        super.onSaveInstanceState(Bundle()) // Чтобы не сохранять открытые фрагменты
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        (supportFragmentManager.findFragmentById(R.id.fragment_container) as? TestProcessFragment)?.abort()
            ?: run {
                super.onBackPressed()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> {
                // showHints()
                when (supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                    is TestProcessFragment -> {
                        Timber.e("Help for process")
                        InfoDialogFragment.createInstance(R.string.hint_test_process).apply {
                            show(supportFragmentManager, tag)
                        }
                    }
                    is TestSettingsFragment -> {
                        Timber.e("Help for settings")
                        InfoDialogFragment.createInstance(R.string.hint_test_settings).apply {
                            show(supportFragmentManager, tag)
                        }
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAsyncOperationError(args: EventArgs<Throwable>) {
        binding.progressBar.changeAppearance(false)
    }

    override fun onBindCards(args: EventArgs<List<CardModel>>) {
        cards = args.content

        binding.progressBar.changeAppearance(false)

        Timber.e("Bind Cards: ${cards?.size}")

        val isDrawAvailable = config.isDrawAnswers

        if ((cards?.size ?: 0) > 0) {
            config.testCounter++
            replaceFragment(
                R.id.fragment_container,
                TestProcessFragment.createInstance(
                    TestProcessModel(
                        verticalSwipe = testSettingsModel.verticalSwipe,
                        testDirection = testSettingsModel.testDirection,
                        size = cards?.size ?: 0,
                        showTranscription = testSettingsModel.showTranscription,
                        playQuestions = testSettingsModel.playQuestion,
                        showDrawButton = isDrawAvailable,
                        showSwipeHint = !isDrawAvailable,
                        ttsAvailable = isTTSAvailable
                    )
                ),
                false
            )
        } else {
            callTestEmptyDialog()
        }
    }

    override fun onBindCollections(args: EventArgs<List<TestCollectionModel>>) {
        val testCollections = args.content

        val introCollection = testCollections.firstOrNull { it.type == CollectionType.EXAMPLE }

        val selectedCollection =
            if (isIntro && introCollection != null) {
                introCollection
            } else if (collection != null) {
                TestCollectionModel(
                    id = collection!!.id,
                    name = collection!!.name,
                    size = collection!!.size()
                )
            } else {
                testCollections.firstOrNull { it.id == config.testCollectionId }
            }

        Timber.e("Config: $config")

        testSettingsModel = TestSettingsModel(
            testCollections,
            selectedCollection,
            testDirection = config.testDirection,
            usePartDefault = false,
            showKnown = config.testShowKnown,
            notShuffle = config.testNotShuffle,
            verticalSwipe = config.testVerticalSwipe,
            showTranscription = config.testShowTranscription,
            playQuestion = config.testPlayQuestions
        )

        if (isIntro) {
            replaceFragment(
                R.id.fragment_container,
                TestSettingsIntroFragment.createInstance(testSettingsModel),
                false
            )
        } else {
            replaceFragment(
                R.id.fragment_container,
                TestSettingsFragment.createInstance(testSettingsModel),
                false
            )
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(!isIntro)
    }

    override fun onTestSettingsReady(model: TestSettingsModel) {
        if (isIntro) {
            Metrics.send(this, InstallTestEvent.START)
        } else {
            Metrics.send(this, TestEvent.START)
        }

        binding.progressBar.changeAppearance(true)

        testSettingsModel = model

        // В мультиязычной верcии повторно инициализируем TTS если язык выбранной коллекции отличается
        if (!resources.getBoolean(R.bool.use_only_language)) {
            // Выбрали коллекцию с языком, отличным от выбранного при старте
            if (model.selectedCollection?.subject != collection?.subject) {
                isTTSAvailable = false
                model.selectedCollection?.run {
                    if (subject != null && subject != LanguageCode.UNDEFINED) {
                        isTTSAvailable = true
                        binding.root.showSnackbar(R.string.book_tts_init)
                        initTTS(subject)
                    }
                }
            }
        }

        model.selectedCollection?.id?.let {
            viewModel.updateCollectionDate(it)
        }

        viewModel.getCardsForTest(
            id = model.selectedCollection?.id,
            shuffle = !model.notShuffle,
            showAll = model.showKnown,
            usePart = model.usePart,
            size = model.size,
            volume = model.volume
        )

        with(config) {
            testCollectionId = model.selectedCollection?.id
            testDirection = model.testDirection
            testNotShuffle = model.notShuffle
            testVerticalSwipe = model.verticalSwipe
            testShowKnown = model.showKnown
            testShowTranscription = model.showTranscription
            testPlayQuestions = model.playQuestion
        }

        if (!isIntro) {
            if (model.testDirection == TestDirection.INVERTED) {
                Metrics.send(this, TestEvent.SETTINGS_INVERTED)
            } else if (model.testDirection == TestDirection.MIXED) Metrics.send(this, TestEvent.SETTINGS_MIXED)

            if (model.usePart) Metrics.send(this, TestEvent.SETTINGS_PART)

            if (model.showKnown) Metrics.send(this, TestEvent.SETTINGS_100)

            if (model.verticalSwipe) Metrics.send(this, TestEvent.SETTINGS_VERTICAL)

            if (model.showTranscription) Metrics.send(this, TestEvent.SETTINGS_TRANSCRIPTION)

            if (model.playQuestion) Metrics.send(this, TestEvent.SETTINGS_SPEAK)

            if (model.notShuffle) Metrics.send(this, TestEvent.SETTINGS_NOSHUFFLE)
        }
    }

    override fun getCardModel(position: Int): CardModel? {
        return cards?.getOrNull(position)
    }

    override fun onSpeak(string: String, showErrors: Boolean) {
        when (ttsStatus) {
            TTS_STATUS_OK -> {
                speechViewModel.speak(string)
                Metrics.send(this, BookEvent.SPEAK)
            }
            TTS_STATUS_NO_DATA -> {
                if (showErrors) showTTSErrorDialog(subjectModel?.name ?: "???")
            }
            TTS_STATUS_NO_LANGUAGE -> {
                if (showErrors) showNoLanguageDialog()
            }
        }
    }

    override fun showDrawDialog(hintOriginal: String, hintTranslation: String, restore: Boolean) {
        if (drawDialogFragment == null || drawDialogFragment?.created == false) {
            val dialogTitle = getString(R.string.test_draw)
            val drawModel = DrawModel(dialogTitle, hintOriginal, hintTranslation)
            drawDialogFragment = DrawDialogFragment.createInstance(drawModel, restore).apply {
                show(supportFragmentManager, tag)
            }
        }
    }

    override fun setCardKnowledge(position: Int, value: Int): Boolean {
        cards?.getOrNull(position)?.let {
            viewModel.updateCardKnowledge(it.id, value)
            return true
        } ?: return false
    }

    override fun onTestFinish(correct: Int, wrong: Int) {
        if (isIntro) Metrics.send(this, InstallTestEvent.RESULTS)
        val showReview = config.testCounter > 5 && !config.isReviewConfirmed
        replaceFragment(
            R.id.fragment_container,
            TestResultFragment.createInstance(correct, wrong, isIntro, showReview = showReview),
            false,
        )
        topMenu?.findItem(R.id.action_help)?.isVisible = false
    }

    override fun copyCardIntent() {
        testSettingsModel.selectedCollection?.id?.let {
            viewModel.getOtherCollections(it)
        } ?: run {
        }
    }

    override fun onBindCopyOptions(args: EventArgs<List<CardAction>>) {
        val options = args.content
        var selectedIndex = 0
        options.forEachIndexed { index, cardAction ->
            if (cardAction.id == config.copyCollectionId) selectedIndex = index
        }
        if (options.isNotEmpty()) {
            callCopyDialog(options, selectedIndex)
        } else {
            callCopyErrorDialog(R.string.context_copy)
        }
    }

    override fun onRepeatTest() {
        replaceFragment(
            R.id.fragment_container,
            TestSettingsFragment.createInstance(testSettingsModel),
            false
        )
        topMenu?.findItem(R.id.action_help)?.isVisible = true
    }

    override fun onLaunchMain() {
        Metrics.send(this, InstallTestEvent.FINISH)
        startActivity(Intent(this, CollectionsActivity::class.java))
        finishAffinity()
    }

    override fun onReviewAction(isAgree: Boolean) {
        config.isReviewConfirmed = true
        if (isAgree) {
            Timber.e("Button YES clicked")
            ReviewManagerFactory.create(this).apply {
                requestReviewFlow().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo: ReviewInfo = task.result
                        val flow: Task<Void> = launchReviewFlow(this@TestActivity, reviewInfo)
                        flow.addOnCompleteListener {
                            Timber.e("Review onComplete: ${it.result} ${it.isSuccessful} ${it.isComplete}")
                        }
                    } else {
                        Timber.e("Is not successful")
                    }
                }
            }
        } else {
            Timber.e("Button NO clicked")
        }
    }

    override fun onCardActionDialogConfirm(action: CardAction) {
        (supportFragmentManager.findFragmentById(R.id.fragment_container) as? TestProcessFragment)?.run {
            cards?.getOrNull(model.position)?.let {
                action.id?.let { it1 ->
                    viewModel.copyCard(it.id, it1)
                    config.copyCollectionId = it1
                }
            }
        }
    }

    override fun onCopyCard(args: EventArgs<Unit>) {
        binding.root.showSnackbar(R.string.snack_copy)
    }

    override fun isIntroState(): Boolean {
        return isIntro
    }

    override fun onDrawDialogDismissed(value: Boolean) {
        drawRestore = value
        (supportFragmentManager.findFragmentById(R.id.fragment_container) as? TestProcessFragment)?.updateDrawPreview(value)
    }

    private fun initComponents() {
        initTTS(collection?.subject)

        with(binding) {
            executePendingBindings()
            invalidateAll()
        }
        setStatusBarColor(R.color.transparent)
        initToolbar(R.id.toolbar, R.string.test_setup)
        initViewModel()

        if (LanguageHelper.synchronizeLocale(this, Locale(config.languageInterface.value))) {
            finish()
            startActivity(intent)
        }
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getCardsLiveData, ::onBindCards)
            success(getCollectionsLiveData, ::onBindCollections)
            success(getOtherCollectionsLiveData, ::onBindCopyOptions)
            success(getCardCopiedLiveData, ::onCopyCard)
        }
        config = viewModel.config
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putSerializable(ExtraKey.COLLECTION.name, collection)
            putBoolean(ExtraKey.IS_INTRO.name, isIntro)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            collection = it.getSerializable(ExtraKey.COLLECTION.name) as? CollectionModel
            isIntro = it.getBoolean(ExtraKey.IS_INTRO.name, false)
        } ?: run {
            intent?.also {
                collection = it.getSerializableExtra(ExtraKey.COLLECTION.name) as? CollectionModel
                isIntro = it.getBooleanExtra(ExtraKey.IS_INTRO.name, false)
            }
        }
    }

    private fun getCollections() {
        viewModel.getCollections()
    }

    private fun callCopyDialog(items: List<CardAction>, selectedIndex: Int = 0) {
        if (dialogFragment == null || dialogFragment?.created == false) {
            dialogFragment = CardActionDialogFragment.createInstance(
                titleId = R.string.context_copy,
                items = items,
                selected = selectedIndex
            ).apply {
                val tag = CardActionDialogFragment::class.java.simpleName
                show(supportFragmentManager, tag)
            }
        }
    }

    private fun callCopyErrorDialog(resId: Int) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(resId)
            .setMessage(R.string.swipe_right_empty)
            .setNegativeButton(R.string.button_ok) { _, _ -> }
            .create()
            .show()
    }

    private fun callTestEmptyDialog() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.test_no_cards)
            .setNegativeButton(R.string.button_ok) { _, _ -> }
            .create()
            .show()
    }

    private fun showNoLanguageDialog() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.lang_error_msg_1)
            .setNegativeButton(R.string.button_ok) { _, _ ->
            }
            .create()
            .show()
    }

    private fun showTTSErrorDialog(lang: String) {
        val msg: String = getString(R.string.lang_error_msg_2, lang)

        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(msg)
            .setPositiveButton(R.string.lang_error_open) { _, _ ->
                startActivity(Intent("com.android.settings.TTS_SETTINGS"))
            }
            .setNegativeButton(R.string.button_cancel) { _, _ ->
            }
            .create()
            .show()
    }

    private fun initTTS(subject: LanguageCode?) {
        Timber.e("Init TTS: $subject")

        val languagesSubject = resources.getStringArray(R.array.languages).toList().map { item ->
            val parts = item.split(":")
            val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
            LanguageModel(name = parts[1], value = defaultValue)
        }

        subjectModel = if (resources.getBoolean(R.bool.use_only_language)) {
            // Один язык - берем параметр из настроек приложения
            val subjectCode = getString(R.string.subject_code)
            languagesSubject.firstOrNull {
                it.value.value == subjectCode
            }
        } else {
            // Много языков - берем параметр из свойств коллекции
            languagesSubject.firstOrNull {
                it.value == subject
            }
        }

        subjectModel?.run {
            textToSpeechEngine = TextToSpeech(this@TestActivity) {
                Timber.e("TextToSpeech is Ready!: $it, language $subjectModel")
                if (it == TextToSpeech.SUCCESS && subjectModel != null) {
                    if (subjectModel!!.value == LanguageCode.UNDEFINED) {
                        ttsStatus = TTS_STATUS_NO_LANGUAGE
                        isTTSAvailable = false
                    } else {
                        val subjectLocale = LanguageHelper.getSubjectLocale(subjectModel!!.value)
                        isTTSAvailable = true
                        val result = textToSpeechEngine?.setLanguage(subjectLocale)
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Timber.e("TTS: The Language not supported! - ${subjectModel?.name}")
                            ttsStatus = TTS_STATUS_NO_DATA
                        } else {
                            speechViewModel.initial(textToSpeechEngine!!, startForResult)
                            ttsStatus = TTS_STATUS_OK
                            Timber.e("TTS Ready")
                        }
                    }
                }
            }
        }
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    .let { text -> text?.get(0) }
            Timber.e("Spoken text: $spokenText")
        }
    }

    companion object {
        const val TTS_STATUS_OK = 0
        const val TTS_STATUS_NO_LANGUAGE = 1
        const val TTS_STATUS_NO_DATA = 2
    }
}
