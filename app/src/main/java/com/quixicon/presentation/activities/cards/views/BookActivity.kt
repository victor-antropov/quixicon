package com.quixicon.presentation.activities.cards.views

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.book.BookEvent
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.showSnackbar
import com.quixicon.core.support.extensions.showToast
import com.quixicon.core.support.extensions.success
import com.quixicon.core.support.helpers.LanguageHelper
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivityBookBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.presentation.activities.cards.adapters.BookAdapter
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.activities.cards.models.BookCardModel
import com.quixicon.presentation.activities.cards.viewmodels.CardsViewModel
import com.quixicon.presentation.activities.cards.viewmodels.SpeechViewModel
import com.quixicon.presentation.activities.collections.models.CollectionModel
import com.quixicon.presentation.activities.editcard.views.EditCardActivity
import com.quixicon.presentation.activities.start.models.LanguageModel
import com.quixicon.presentation.activities.test.views.TestActivity
import com.quixicon.presentation.fragments.bookcard.views.BookCardFragment
import com.quixicon.presentation.fragments.cardaction.views.CardActionDialogFragment
import com.quixicon.presentation.fragments.draw.models.DrawModel
import com.quixicon.presentation.fragments.draw.views.DrawDialogFragment
import com.quixicon.presentation.views.AsyncOperationView
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

class BookActivity :
    DaggerAppCompatActivity(),
    AsyncOperationView,
    BookView,
    BookCardFragment.Listener,
    CardActionDialogFragment.Listener,
    DrawDialogFragment.Listener {

    private lateinit var binding: ActivityBookBinding

    private lateinit var viewModel: CardsViewModel

    private lateinit var adapter: BookAdapter

    private lateinit var collection: CollectionModel

    private lateinit var sortOrder: CardSortOrder

    private var cardId: Long = 0

    private var startPosition: Int? = null

    private var dialogFragment: CardActionDialogFragment? = null

    private var copyOptions = listOf<CardAction>()

    private lateinit var config: QuixiconConfig

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val speechViewModel: SpeechViewModel by viewModels()

    private var drawRestore: Boolean = false

    var subjectModel: LanguageModel? = null

    private var ttsStatus: Int = TTS_STATUS_OK

    private var isTTSAvailable: Boolean = false

    private var isTTSInitShown: Boolean = false

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            startPosition = position
            drawRestore = false
            super.onPageSelected(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_book)
        initComponents()
        getOtherCollections()
    }

    override fun onPause() {
        viewModel.config = config
        super.onPause()
    }

    override fun onResume() {
        config = viewModel.config
        getCards()
        super.onResume()
    }

    override fun onDestroy() {
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        speechViewModel.destroy()
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
        binding.progressBar.changeAppearance(false)
    }

    override fun onAsyncOperationProgress(args: EventArgs<Boolean>) {
    }

    override fun onBindBookCards(args: EventArgs<List<BookCardModel>>) {
        val cards = args.content
        cards.forEach {
            it.showSpeaker = false
        }
        binding.progressBar.changeAppearance(false)
        if (cards.isNotEmpty()) {
            val pos = cards.indexOfFirst { it.id == cardId }
            adapter.update(cards)
            if (startPosition == null) {
                if (pos != -1) {
                    startPosition = pos
                    binding.viewPager.setCurrentItem(pos, false)
                }
            } else {
                binding.viewPager.setCurrentItem(startPosition!!, false)
            }
        }
    }

    override fun onBindCopyOptions(args: EventArgs<List<CardAction>>) {
        copyOptions = args.content
    }

    override fun onActionSelected(model: BookCardModel, action: CardActionType) {
        when (action) {
            CardActionType.COPY -> {
                if (copyOptions.isNotEmpty()) {
                    callCopyDialog(copyOptions)
                } else {
                    callCopyErrorDialog(R.string.context_copy)
                }
            }
            CardActionType.SET_0 -> {
                viewModel.updateCardKnowledge(model.id, 0)
                adapter.updateKnowledge(model, 0)
            }
            CardActionType.SET_100 -> {
                viewModel.updateCardKnowledge(model.id, 100)
                adapter.updateKnowledge(model, 100)
            }
            CardActionType.EDIT -> {
                startActivity(
                    Intent(this, EditCardActivity::class.java).apply {
                        putExtra(ExtraKey.CARD_ID.name, model.id)
                    },
                )
            }
            else -> {
            }
        }
    }

    override fun onSpeak(string: String) {
        when (ttsStatus) {
            TTS_STATUS_OK -> {
                if (isTTSAvailable && !isTTSInitShown) {
                    showToast(R.string.book_tts_init, Toast.LENGTH_SHORT)
                    isTTSInitShown = true
                }
                speechViewModel.speak(string)
                Metrics.send(this, BookEvent.SPEAK)
            }
            TTS_STATUS_NO_DATA -> {
                showTTSErrorDialog(subjectModel?.name ?: "???")
            }
            TTS_STATUS_NO_LANGUAGE -> {
                showNoLanguageDialog()
            }
        }
    }

    override fun onFlipBack() {
        val position = binding.viewPager.currentItem - 1

        if (position >= 0) {
            binding.viewPager.setCurrentItem(position, true)
            drawRestore = false
        }
    }

    override fun onFlipForward() {
        val size = binding.viewPager.adapter?.itemCount ?: 0
        val position = binding.viewPager.currentItem + 1

        if (position < size) {
            binding.viewPager.setCurrentItem(position, true)
            drawRestore = false
        }
    }

    override fun onCardActionDialogConfirm(action: CardAction) {
        val position = binding.viewPager.currentItem
        adapter.getItem(position)?.run {
            action.id?.let {
                viewModel.copyCard(id, it)
                config.copyCollectionId = it
            }
        }
    }

    override fun onCopyCard(args: EventArgs<Unit>) {
        binding.root.showSnackbar(R.string.snack_copy)
    }

    override fun onUpdateCard(args: EventArgs<Unit>) {}

    override fun onDrawDialogDismissed(value: Boolean) {
        drawRestore = value
    }

    private fun initComponents() {
        val languagesSubject = resources.getStringArray(R.array.languages).toList().map { item ->
            val parts = item.split(":")
            val defaultValue = enumValues<LanguageCode>().firstOrNull() { it.value == parts[0] } ?: LanguageCode.EN
            LanguageModel(name = parts[1], value = defaultValue)
        }

        val isMultiLanguages = !resources.getBoolean(R.bool.use_only_language)

        subjectModel = if (!isMultiLanguages) {
            // Один язык - берем параметр из настроек приложения
            val subjectCode = getString(R.string.subject_code)
            languagesSubject.firstOrNull {
                it.value.value == subjectCode
            }
        } else {
            // Много языков - берем параметр из свойств коллекции
            languagesSubject.firstOrNull {
                it.value == collection.subject
            }
        }

        initViewModel()
        with(binding) {
            executePendingBindings()
            invalidateAll()
        }
        setStatusBarColor(R.color.transparent)
        initToolbar(R.id.toolbar, collection.name)

        isTTSAvailable = true
        if (isMultiLanguages) {
            if (subjectModel == null || subjectModel?.value == LanguageCode.UNDEFINED) isTTSAvailable = false
        }

        initAdapter()

        if (collection.superType != null) {
            binding.btnTest.hide()
        } else {
            binding.btnTest.show()
        }

        binding.btnTest.setOnClickListener {
            Metrics.send(this, BookEvent.TEST)
            startTestActivity()
        }

        val isDrawAvailable = config.isDrawAnswers

        if (isDrawAvailable) {
            binding.btnDraw.changeAppearance(true)
            binding.btnDraw.setOnClickListener {
                val dialogTitle = getString(R.string.book_draw)
                val position = binding.viewPager.currentItem
                adapter.getItem(position)?.run {
                    val model = DrawModel(dialogTitle, original, answer ?: "")
                    DrawDialogFragment.createInstance(model, drawRestore).apply { show(supportFragmentManager, tag) }
                }
            }
        } else {
            binding.btnDraw.changeAppearance(false)
        }

        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getBookCardsLiveData, ::onBindBookCards)
            success(getOtherCollectionsLiveData, ::onBindCopyOptions)
            success(getCardCopiedLiveData, ::onCopyCard)
            success(getCardUpdateLiveData, ::onUpdateCard)
        }
        config = viewModel.config

        subjectModel?.run {
            speechViewModel.initial(textToSpeechEngine, startForResult)
        }
    }

    private fun initAdapter() {
        adapter = BookAdapter(this, isTTSAvailable)

        binding.viewPager.let {
            it.adapter = adapter
            it.currentItem = 0
        }
    }

    private fun getCards() {
        binding.progressBar.changeAppearance(true)
        val filter = if (config.useFilter) config.languageSubject else null
        collection.superType?.let {
            viewModel.getBookCards(it, sortOrder, filter)
        } ?: run {
            viewModel.getBookCards(collection.id, sortOrder, filter)
        }
    }

    private fun getOtherCollections() {
        viewModel.getOtherCollections(collection.id)
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putSerializable(ExtraKey.COLLECTION.name, collection)
            putSerializable(ExtraKey.ORDER.name, sortOrder)
            putLong(ExtraKey.CARD_ID.name, cardId)
            startPosition?.let { putInt(START_PAGE, it) }
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            collection = it.getSerializable(ExtraKey.COLLECTION.name) as CollectionModel
            sortOrder = it.getSerializable(ExtraKey.ORDER.name) as CardSortOrder
            cardId = it.getLong(ExtraKey.CARD_ID.name)
            if (it.containsKey(START_PAGE)) startPosition = it.getInt(START_PAGE)
        } ?: run {
            intent?.also {
                collection = it.getSerializableExtra(ExtraKey.COLLECTION.name) as CollectionModel
                sortOrder = it.getSerializableExtra(ExtraKey.ORDER.name) as CardSortOrder
                cardId = it.getLongExtra(ExtraKey.CARD_ID.name, 0)
            }
        }
    }

    private fun callCopyDialog(items: List<CardAction>) {
        if (dialogFragment == null || dialogFragment?.created == false) {
            var selectedIndex = 0
            items.forEachIndexed { index, cardAction ->
                if (cardAction.id == config.copyCollectionId) selectedIndex = index
            }
            dialogFragment = CardActionDialogFragment.createInstance(
                titleId = R.string.context_copy,
                items = items,
                selected = selectedIndex,
            ).apply {
                val tag = CardActionDialogFragment::class.java.simpleName
                show(supportFragmentManager, tag)
            }
        }
    }

    private fun startTestActivity() {
        if (collection.id > 0) {
            startActivity(
                Intent(this, TestActivity::class.java).apply {
                    putExtra(ExtraKey.COLLECTION.name, collection)
                },
            )
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

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    .let { text -> text?.get(0) }
            Timber.e("Spoken text: $spokenText")
        }
    }

    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this) {
            Timber.e("TextToSpeech is Ready!: $it, language $subjectModel")
            if (it == TextToSpeech.SUCCESS && subjectModel != null) {
                if (subjectModel!!.value == LanguageCode.UNDEFINED) {
                    ttsStatus = TTS_STATUS_NO_LANGUAGE
                } else {
                    val subjectLocale = LanguageHelper.getSubjectLocale(subjectModel!!.value)
                    Timber.e("Set Subject Locale: $subjectLocale")
                    val result = textToSpeechEngine.setLanguage(subjectLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Timber.e("TTS: The Language not supported! - ${subjectModel?.name}")
                        ttsStatus = TTS_STATUS_NO_DATA
                    } else {
                        Timber.e("TTS Ready")
                        ttsStatus = TTS_STATUS_OK
                    }
                }
            }
        }
    }

    companion object {
        const val START_PAGE = "START_PAGE"
        const val TTS_STATUS_OK = 0
        const val TTS_STATUS_NO_LANGUAGE = 1
        const val TTS_STATUS_NO_DATA = 2
    }
}
