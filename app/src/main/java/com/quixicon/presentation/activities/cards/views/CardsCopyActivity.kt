package com.quixicon.presentation.activities.cards.views

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quixicon.BR
import com.quixicon.R
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.success
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivityCardsCopyBinding
import com.quixicon.databinding.RowCardBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.presentation.activities.cards.adapters.CardsExtendedAdapter
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.activities.cards.entities.CardSwipeMode
import com.quixicon.presentation.activities.cards.helpers.CardTouchHelperCallback
import com.quixicon.presentation.activities.cards.models.CardModel
import com.quixicon.presentation.activities.cards.viewmodels.CardsViewModel
import com.quixicon.presentation.activities.editcard.views.EditCardActivity
import com.quixicon.presentation.views.AsyncOperationView
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

class CardsCopyActivity :
    DaggerAppCompatActivity(),
    AsyncOperationView,
    CardsExtendedAdapter.OnItemActionListener,
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    CardsCopyView {

    private lateinit var binding: ActivityCardsCopyBinding

    private lateinit var viewModel: CardsViewModel

    private lateinit var adapter: CardsExtendedAdapter

    private lateinit var touchCallback: CardTouchHelperCallback

    private var actionCopy: CardAction? = null

    private var collectionId: Long = 0

    private var filterSubject: LanguageCode? = null

    private lateinit var config: QuixiconConfig

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        Timber.e("Create Activity")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cards_copy)
        if (collectionId == 0L) finish()
        initViewModel()
        initComponents()
    }

    override fun onDestroy() {
        Timber.e("Destroy Activity")
        super.onDestroy()
    }

    override fun onResume() {
        getCards()
        super.onResume()
    }

    override fun onPause() {
        viewModel.config = config
        super.onPause()
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
        val errorMessage = args.content.message
        Timber.e("ERROR: $errorMessage")
    }

    override fun onBindCards(args: EventArgs<List<CardModel>>) {
        val cards = args.content
        binding.progressBar.changeAppearance(false)
        if (cards.isNotEmpty()) {
            Timber.e("BIND cards: ${cards.size}")
            adapter.update(cards)
        }
    }

    override fun onItemClick(variable: CardModel?, binding: RowCardBinding?) {}

    override fun onItemSwipe(variable: CardModel?, direction: Int) {
        val card = variable!!
        actionCopy?.let { viewModel.processAction(card.id, it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort_az -> {
                config.cardSortOrder = CardSortOrder.AZ
                item.isChecked = true
                getCards()
            }
            R.id.action_sort_date_asc -> {
                config.cardSortOrder = CardSortOrder.OLDEST
                item.isChecked = true
                getCards()
            }
            R.id.action_sort_date_desc -> {
                config.cardSortOrder = CardSortOrder.RECENT
                item.isChecked = true
                getCards()
            }
            R.id.action_sort_default -> {
                config.cardSortOrder = CardSortOrder.DEFAULT
                item.isChecked = true
                getCards()
            }
            R.id.action_sort_rotation_asc -> {
                config.cardSortOrder = CardSortOrder.UNKNOWN
                item.isChecked = true
                getCards()
            }
            R.id.action_sort_rotation_desc -> {
                config.cardSortOrder = CardSortOrder.KNOWN
                item.isChecked = true
                getCards()
            }
            R.id.action_sort_type -> {
                config.cardSortOrder = CardSortOrder.TYPE
                item.isChecked = true
                getCards()
            }
            R.id.action_filter -> {
                showFilterDialog()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cards_copy, menu)
        val searchView = menu.findItem(R.id.action_search)!!.actionView!! as SearchView
        initSearchView(searchView)

        val itemId = when (config.cardSortOrder) {
            CardSortOrder.DEFAULT -> R.id.action_sort_default
            CardSortOrder.AZ -> R.id.action_sort_az
            CardSortOrder.RECENT -> R.id.action_sort_date_desc
            CardSortOrder.OLDEST -> R.id.action_sort_date_asc
            CardSortOrder.TYPE -> R.id.action_sort_type
            CardSortOrder.UNKNOWN -> R.id.action_sort_rotation_asc
            CardSortOrder.KNOWN -> R.id.action_sort_rotation_desc
        }

        menu.findItem(itemId)?.isChecked = true

        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (::adapter.isInitialized) {
            adapter.filter.filter(newText)
        }
        return false
    }

    override fun onClose(): Boolean {
        if (::adapter.isInitialized) {
            adapter.filter.filter("")
        }
        return false
    }

    private fun initComponents() {
        setStatusBarColor(R.color.transparent)
        initToolbar(R.id.toolbar, "")

        val isMultiLanguage = !resources.getBoolean(R.bool.use_only_language)

        adapter = CardsExtendedAdapter(
            this,
            arrayListOf(),
            BR.cardModel,
            R.layout.row_card,
            BR.onClickListener,
            this,
            BR.editModel,
            typeFilter = mutableMapOf(CardType.WORD to config.filterWords, CardType.PHRASE to config.filterPhrases, CardType.RULE to config.filterRules),
        ).apply {
            editMode = CardSwipeMode.RIGHT
            showLanguage = isMultiLanguage && !config.useFilter
        }

        touchCallback = CardTouchHelperCallback(adapter, false).apply {
            swipeEnable = true
        }

        with(binding) {
            rvCards.let {
                it.layoutManager =
                    LinearLayoutManager(this@CardsCopyActivity, RecyclerView.VERTICAL, false)
                it.adapter = adapter
                ItemTouchHelper(touchCallback).apply {
                    attachToRecyclerView(it)
                }
            }
            btnNewCard.setOnClickListener {
                startActivity(
                    Intent(this@CardsCopyActivity, EditCardActivity::class.java).apply {
                        putExtra(ExtraKey.COLLECTION_ID.name, collectionId)
                    },
                )
                finish()
            }
            executePendingBindings()
            invalidateAll()
        }
        actionCopy = CardAction(action = CardActionType.COPY, id = collectionId)
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getCardsLiveData, ::onBindCards)
        }
        config = viewModel.config
    }

    private fun initSearchView(searchView: SearchView) {
        with(searchView) {
            queryHint = getString(R.string.action_search)
            maxWidth = Integer.MAX_VALUE
            setOnQueryTextListener(this@CardsCopyActivity)
            setOnCloseListener(this@CardsCopyActivity)
        }
    }

    private fun getCards() {
        binding.progressBar.changeAppearance(true)
        viewModel.getAllCards(config.cardSortOrder, filterSubject)
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putLong(ExtraKey.COLLECTION_ID.name, collectionId)
            putSerializable(ExtraKey.FILTER.name, filterSubject)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            collectionId = it.getLong(ExtraKey.COLLECTION_ID.name)
            filterSubject = it.getSerializable(ExtraKey.FILTER.name) as? LanguageCode?
        } ?: run {
            intent?.also {
                collectionId = it.getLongExtra(ExtraKey.COLLECTION_ID.name, 0)
                filterSubject = it.getSerializableExtra(ExtraKey.FILTER.name) as? LanguageCode?
            }
        }
    }

    private fun showFilterDialog() {
        val items = arrayOf(
            getString(R.string.all_words),
            getString(R.string.all_phrases),
            getString(R.string.all_rules),
        )
        val states = booleanArrayOf(config.filterWords, config.filterPhrases, config.filterRules)

        val listener = DialogInterface.OnMultiChoiceClickListener { _, position, state ->
            when (position) {
                0 -> {
                    config.filterWords = state
                }
                1 -> {
                    config.filterPhrases = state
                }
                2 -> {
                    config.filterRules = state
                }
            }
        }

        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setMultiChoiceItems(items, states, listener)
            .setNegativeButton(R.string.button_ok) { _, _ -> }
            .setOnDismissListener {
                applyFilter()
            }
            .create()
            .show()
    }

    private fun applyFilter() {
        adapter.typeFilter = mutableMapOf(CardType.WORD to config.filterWords, CardType.PHRASE to config.filterPhrases, CardType.RULE to config.filterRules)
        adapter.filter.filter("")
    }
}
