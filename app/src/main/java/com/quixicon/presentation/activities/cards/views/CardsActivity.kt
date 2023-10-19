package com.quixicon.presentation.activities.cards.views

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.quixicon.BR
import com.quixicon.BuildConfig
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.cards.CardsEvent
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.showSnackbar
import com.quixicon.core.support.extensions.success
import com.quixicon.core.support.helpers.HtmlHelper
import com.quixicon.core.support.listeners.LinkListener
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivityCardsBinding
import com.quixicon.databinding.RowCardBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.presentation.activities.cards.adapters.CardsExtendedAdapter
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.activities.cards.entities.CardActionType
import com.quixicon.presentation.activities.cards.entities.CardSwipeMode
import com.quixicon.presentation.activities.cards.helpers.CardTouchHelperCallback
import com.quixicon.presentation.activities.cards.models.CardModel
import com.quixicon.presentation.activities.cards.models.CardsEditModel
import com.quixicon.presentation.activities.cards.viewmodels.CardsViewModel
import com.quixicon.presentation.activities.collections.models.CollectionModel
import com.quixicon.presentation.activities.editcard.views.EditCardActivity
import com.quixicon.presentation.activities.test.views.TestActivity
import com.quixicon.presentation.fragments.cardaction.views.CardActionDialogFragment
import com.quixicon.presentation.fragments.selectdialog.views.AddCardDialogFragment
import com.quixicon.presentation.views.AsyncOperationView
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

class CardsActivity :
    DaggerAppCompatActivity(),
    AsyncOperationView,
    CardsExtendedAdapter.OnItemActionListener,
    CardActionDialogFragment.Listener,
    View.OnClickListener,
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    LinkListener,
    CardsView {

    private lateinit var binding: ActivityCardsBinding

    private lateinit var viewModel: CardsViewModel

    private lateinit var adapter: CardsExtendedAdapter

    private lateinit var touchCallback: CardTouchHelperCallback

    private lateinit var collection: CollectionModel

    private var dialogFragment: CardActionDialogFragment? = null

    private var actionUpdate: CardAction = CardAction(CardActionType.SET_100)
    private var actionCopy: CardAction? = CardAction(CardActionType.COPY)

    private var actionLeftOptions = mutableListOf<CardAction>()
    private var actionRightOptions = mutableListOf<CardAction>()

    private lateinit var config: QuixiconConfig

    private var tempCardId: Long? = null

    private var swipeModeDefault = false

    private var balloons = mutableListOf<Balloon>()

    private var isTestButtonVisible: Boolean = true
    private var isAddButtonVisible: Boolean = true

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        Timber.e("Create Activity")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cards)
        initViewModel()
        initComponents()
        initData()
        getOtherCollections()
        updateCollectionDate(collection)
    }

    override fun onDestroy() {
        Timber.e("Destroy Activity")
        super.onDestroy()
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

    override fun onAsyncOperationProgress(args: EventArgs<Boolean>) {
    }

    override fun onBindCards(args: EventArgs<List<CardModel>>) {
        val cards = args.content
        binding.progressBar.changeAppearance(false)
        if (!cards.isNullOrEmpty()) {
            adapter.update(cards)
            binding.editModel?.isEmpty = false
            showTestButton(true)
            showMainHints()
        } else {
            binding.editModel?.isEmpty = true
            showTestButton(false)
        }
    }

    override fun onBindCopyOptions(args: EventArgs<List<CardAction>>) {
        val options = args.content
        actionRightOptions.clear()
        actionRightOptions.addAll(options)
        actionCopy = options.firstOrNull { it.id == config.copyCollectionId } ?: options.getOrNull(0)
        binding.editModel?.rightActionName = actionCopy?.name ?: ""
    }

    override fun onGetLinkedCollections(args: EventArgs<List<String>>) {
        val names = args.content
        callInfoDialog(names)
    }

    override fun onCardDeleted(args: EventArgs<Unit>) {
        if (adapter.itemCount == 0) binding.editModel?.isEmpty = true
    }

    override fun onItemClick(variable: CardModel?, binding: RowCardBinding?) {
        val card = variable!!
        Metrics.send(baseContext, CardsEvent.SELECT)
        startActivity(
            Intent(this, BookActivity::class.java).apply {
                putExtra(ExtraKey.COLLECTION.name, collection)
                putExtra(ExtraKey.CARD_ID.name, card.id)
                putExtra(ExtraKey.ORDER.name, config.cardSortOrder)
            }
        )
    }

    override fun onItemLongClick(variable: CardModel?, binding: RowCardBinding?) {
        if (variable == null || binding == null) return

        val view = binding.ivRotation

        Metrics.send(baseContext, CardsEvent.CONTEXT_MENU)

        PopupMenu(this, view).apply {
            inflate(R.menu.menu_card_context)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_edit -> {
                        startActivity(
                            Intent(
                                this@CardsActivity,
                                EditCardActivity::class.java
                            ).apply {
                                putExtra(ExtraKey.CARD_ID.name, variable.id)
                            }
                        )
                    }
                    R.id.item_delete -> {
                        callDeleteDialog(variable)
                    }
                    R.id.item_set_0 -> {
                        viewModel.updateCardKnowledge(variable.id, 0)
                        adapter.updateKnowledge(variable, 0)
                    }
                    R.id.item_set_100 -> {
                        viewModel.updateCardKnowledge(variable.id, 100)
                        adapter.updateKnowledge(variable, 100)
                    }
                    R.id.item_copy -> {
                        if (actionRightOptions.size > 0) {
                            callCopyDialog(actionRightOptions, variable.id)
                        } else {
                            callCopyErrorDialog(R.string.context_copy)
                        }
                    }
                    R.id.item_info -> {
                        viewModel.getLinkedCollections(variable.id)
                    }
                }
                true
            }
            show()
        }
    }

    override fun onItemSwipe(variable: CardModel?, direction: Int) {
        val card = variable!!
        if (direction == ItemTouchHelper.START) {
            Metrics.send(baseContext, CardsEvent.SWIPE_LEFT)
            viewModel.processAction(card.id, actionUpdate)
        } else if (direction == ItemTouchHelper.END) {
            Metrics.send(baseContext, CardsEvent.SWIPE_RIGHT)
            actionCopy?.let { viewModel.processAction(card.id, it) } ?: run { callCopyErrorDialog(R.string.swipe_right) }
        }
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

            R.id.action_add -> {
                if (collection.superType == null) {
                    Metrics.send(this, CardsEvent.TOP_ADD)
                    AddCardDialogFragment.createInstance().apply { show(supportFragmentManager, tag) }
                }
            }

            R.id.action_help -> {
                showMainHints(showAlways = true)
                showSwipeHints(showAlways = true)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cards, menu)
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

        if (collection.id == 0L) menu.findItem(R.id.action_add).isVisible = false

        return true
    }

    override fun onCardActionDialogConfirm(action: CardAction) {
        if (action.action == CardActionType.COPY) {
            val tag = dialogFragment?.tag
            if (tag == DialogType.SINGLE_COPY.name) {
                if (action.id != null && tempCardId != null) {
                    viewModel.copyCard(tempCardId!!, action.id)
                    binding.root.showSnackbar(R.string.snack_copy)
                    config.copyCollectionId = action.id
                }
            } else {
                actionCopy = action
                binding.editModel?.rightActionName = action.name
                config.copyCollectionId = action.id
            }
        } else {
            actionUpdate = action
            binding.editModel?.leftActionName = action.name
            adapter.actionType = action.action
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        binding.layoutToolbar.setExpanded(false)
        showTestButton(false)
        showAddButton(false)
        if (::adapter.isInitialized) {
            adapter.filter.filter(newText)
        }
        return false
    }

    override fun onClose(): Boolean {
        showTestButton(true)
        showAddButton(true)
        if (::adapter.isInitialized) {
            adapter.filter.reset()
        }
        return false
    }

    override fun onLinkAction(value: String) {
        when (value) {
            "NEW_CARD" -> {
                if (collection.superType == null) {
                    startActivity(
                        Intent(this@CardsActivity, EditCardActivity::class.java).apply {
                            putExtra(ExtraKey.COLLECTION_ID.name, collection.id)
                        }
                    )
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.item_copy -> {
                val isMultiLanguage = !resources.getBoolean(R.bool.use_only_language)
                val filter = when {
                    !isMultiLanguage -> null
                    config.useFilter -> config.languageSubject
                    else -> collection.subject
                }
                startActivity(
                    Intent(this@CardsActivity, CardsCopyActivity::class.java).apply {
                        putExtra(ExtraKey.COLLECTION_ID.name, collection.id)
                        putExtra(ExtraKey.FILTER.name, filter)
                    }
                )
            }
            R.id.item_new -> {
                startActivity(
                    Intent(this@CardsActivity, EditCardActivity::class.java).apply {
                        putExtra(ExtraKey.COLLECTION_ID.name, collection.id)
                    }
                )
            }
            R.id.btn_add -> {
                Metrics.send(this, CardsEvent.FAB_ADD)
                AddCardDialogFragment.createInstance().apply { show(supportFragmentManager, tag) }
            }
        }
    }

    private fun initData() {
        actionLeftOptions = arrayListOf(
            CardAction(CardActionType.SET_100, getString(R.string.option_set_100)),
            CardAction(CardActionType.SET_0, getString(R.string.option_set_0)),
            CardAction(CardActionType.DELETE, getString(R.string.option_delete), collection.id)
        )
        actionUpdate = actionLeftOptions[0]

        binding.editModel?.leftActionName = actionUpdate.name
    }

    private fun initComponents() {
        setStatusBarColor(R.color.transparent)
        initToolbar(R.id.toolbar, "")

        showAddButton(true)

        val isMultiLanguage = !resources.getBoolean(R.bool.use_only_language)

        adapter = CardsExtendedAdapter(
            this,
            arrayListOf(),
            BR.cardModel,
            R.layout.row_card,
            BR.onClickListener,
            this,
            BR.editModel,
            BR.onLongClickListener
        ).apply {
            editMode = if (swipeModeDefault) CardSwipeMode.ENABLED else CardSwipeMode.DISABLED
            showLanguage = (isMultiLanguage && !config.useFilter) || BuildConfig.DEBUG
        }

        touchCallback = CardTouchHelperCallback(adapter).apply {
            swipeEnable = swipeModeDefault
        }

        with(binding) {
            collectionModel = collection
            editModel = CardsEditModel(swipeModeDefault)

            HtmlHelper.setHtmlWithLinkClickHandler(tvInfo, getString(R.string.catalog_empty), this@CardsActivity)

            rvCards.let {
                it.layoutManager =
                    LinearLayoutManager(this@CardsActivity, RecyclerView.VERTICAL, false)
                it.adapter = adapter
                ItemTouchHelper(touchCallback).apply {
                    attachToRecyclerView(it)
                }
                registerForContextMenu(it)
            }

            switchMode.setOnClickListener {
                val state = (it as SwitchMaterial).isChecked
                adapter.editMode = if (state) CardSwipeMode.ENABLED else CardSwipeMode.DISABLED
                touchCallback.swipeEnable = state
                if (state) {
                    showSwipeHints()
                    Metrics.send(baseContext, CardsEvent.SWIPE_ON)
                }
            }

            tvActionLeft.setOnClickListener {
                Metrics.send(baseContext, CardsEvent.MENU_LEFT)
                callSwipeDialog(DialogType.SWIPE_UPDATE)
            }

            tvActionRight.setOnClickListener {
                Metrics.send(baseContext, CardsEvent.MENU_RIGHT)
                if (actionRightOptions.size > 0) {
                    callSwipeDialog(DialogType.SWIPE_COPY)
                } else {
                    callCopyErrorDialog(R.string.swipe_right)
                }
            }

            btnTest.setOnClickListener {
                Metrics.send(baseContext, CardsEvent.FAB_TEST)
                startTestActivity()
            }

            btnAdd.setOnClickListener(this@CardsActivity)

            executePendingBindings()
            invalidateAll()
        }
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getCardsLiveData, ::onBindCards)
            success(getOtherCollectionsLiveData, ::onBindCopyOptions)
            success(getLinkedCollectionsLiveData, ::onGetLinkedCollections)
            success(getCardDeletedLiveData, ::onCardDeleted)
        }
        config = viewModel.config
    }

    private fun initSearchView(searchView: SearchView) {
        with(searchView) {
            queryHint = getString(R.string.action_search)
            maxWidth = Integer.MAX_VALUE
            setOnQueryTextListener(this@CardsActivity)
            setOnCloseListener(this@CardsActivity)
            setOnQueryTextFocusChangeListener { _, b ->
                if (b) {
                    binding.layoutToolbar.setExpanded(false)
                    showTestButton(false)
                    showAddButton(false)
                    Metrics.send(baseContext, CardsEvent.TOP_SEARCH)
                }
            }
        }
    }

    private fun getCards() {
        binding.progressBar.changeAppearance(true)
        val filter = if (config.useFilter) config.languageSubject else null
        collection.superType?.let {
            viewModel.getCards(it, config.cardSortOrder, filter)
        } ?: run {
            viewModel.getCards(collection.id, config.cardSortOrder, filter)
        }
    }

    private fun getOtherCollections() {
        viewModel.getOtherCollections(collection.id)
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putSerializable(ExtraKey.COLLECTION.name, collection)
            swipeModeDefault = binding.editModel?.swipeMode ?: false
            putBoolean(SWIPE_MODE, swipeModeDefault)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            collection = it.getSerializable(ExtraKey.COLLECTION.name) as CollectionModel
            swipeModeDefault = it.getBoolean(SWIPE_MODE, false)
        } ?: run {
            intent?.also {
                collection = it.getSerializableExtra(ExtraKey.COLLECTION.name) as CollectionModel
            }
        }
    }

    private fun callSwipeDialog(type: DialogType) {
        if (dialogFragment == null || dialogFragment?.created == false) {
            dialogFragment = CardActionDialogFragment.createInstance(
                titleId = if (type == DialogType.SWIPE_UPDATE) R.string.swipe_left else R.string.swipe_right,
                subtitleId = if (type == DialogType.SWIPE_UPDATE) R.string.select_action else R.string.copy_to,
                items = if (type == DialogType.SWIPE_UPDATE) actionLeftOptions else actionRightOptions,
                selected = if (type == DialogType.SWIPE_UPDATE) 0 else actionRightOptions.indexOf(actionCopy)
            ).apply {
                val tag = type.name
                show(supportFragmentManager, tag)
            }
        }
    }

    private fun callCopyDialog(items: List<CardAction>, cardId: Long) {
        if (dialogFragment == null || dialogFragment?.created == false) {
            tempCardId = cardId
            var selectedIndex = 0
            items.forEachIndexed { index, cardAction ->
                if (cardAction.id == config.copyCollectionId) selectedIndex = index
            }
            dialogFragment = CardActionDialogFragment.createInstance(
                titleId = R.string.context_copy,
                items = items,
                selected = selectedIndex
            ).apply {
                val tag = DialogType.SINGLE_COPY.name
                show(supportFragmentManager, tag)
            }
        }
    }

    private fun callDeleteDialog(card: CardModel) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(R.string.delete_title)
            .setMessage(
                if (collection.superType == null) {
                    R.string.delete_card
                } else {
                    R.string.delete_card_super
                }
            )
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteCard(card.id, collection.id)
                adapter.removeCardItem(card)
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .create()
            .show()
    }

    private fun callInfoDialog(names: List<String>) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(R.string.info_collections_title)
            .setItems(names.toTypedArray()) { _, _ -> }
            .setNegativeButton(R.string.button_ok) { _, _ -> }
            .create()
            .show()
    }

    private fun callCopyErrorDialog(resId: Int) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setTitle(resId)
            .setMessage(R.string.swipe_right_empty)
            .setNegativeButton(R.string.button_ok) { _, _ -> }
            .create()
            .show()
    }

    private fun startTestActivity() {
        startActivity(
            Intent(this, TestActivity::class.java).apply {
                putExtra(ExtraKey.COLLECTION.name, collection)
            }
        )
    }

    private fun updateCollectionDate(collection: CollectionModel) {
        if (collection.superType == null) viewModel.updateCollectionDate(collection.id)
    }

    private fun showTestButton(state: Boolean) {
        if (collection.superType == null) {
            if (state) {
                binding.btnTest.show()
                isTestButtonVisible = true
            } else {
                binding.btnTest.hide()
                isTestButtonVisible = false
            }
        } else {
            binding.btnTest.hide()
            isTestButtonVisible = false
        }
    }

    private fun showAddButton(state: Boolean) {
        if (collection.superType == null) {
            if (state) {
                binding.btnAdd.show()
                isAddButtonVisible = true
            } else {
                binding.btnAdd.hide()
                isAddButtonVisible = false
            }
        } else {
            binding.btnAdd.hide()
            isAddButtonVisible = false
        }
    }

    private fun showMainHints(showAlways: Boolean = false) {
        if (config.hintCards || showAlways) {
            Metrics.send(this, CardsEvent.SHOW_HINTS)
            balloons.add(showBalloon(binding.switchMode, getString(R.string.hint_swipe), BALLOON_TOP))
            if (isAddButtonVisible) balloons.add(showBalloon(binding.btnAdd, getString(R.string.hint_card)))
            if (isTestButtonVisible) balloons.add(showBalloon(binding.btnTest, getString(R.string.hint_test)))
            balloons.add(showBalloon(binding.rvCards, getString(R.string.hint_card_menu), BALLOON_CENTER))
        }
        config.hintCards = false
    }

    private fun showSwipeHints(showAlways: Boolean = false) {
        if (config.hintSwipe || showAlways) {
            if (binding.editModel?.swipeMode == true) {
                balloons.add(showBalloon(binding.tvActionLeft, getString(R.string.hint_action), BALLOON_BOTTOM))
                config.hintSwipe = false
            }
        }
    }

    private fun showBalloon(view: View, title: String, direction: Int = BALLOON_LEFT): Balloon {
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
            setArrowOrientation(
                when (direction) {
                    BALLOON_TOP -> ArrowOrientation.BOTTOM
                    BALLOON_BOTTOM -> ArrowOrientation.TOP
                    BALLOON_CENTER -> ArrowOrientation.BOTTOM
                    else -> ArrowOrientation.END
                }
            )
            setArrowAlignAnchorPadding(-10)
            setIsVisibleArrow(direction != BALLOON_CENTER)
            setLifecycleOwner(this@CardsActivity)
            setTextColor(color)
            setBackgroundColor(background)
            setDismissWhenClicked(false)
            setOnBalloonClickListener { dismissAllBallons() }
            setOnBalloonOutsideTouchListener { _, _ -> dismissAllBallons() }
        }.apply {
            when (direction) {
                BALLOON_TOP -> showAlignTop(view)
                BALLOON_BOTTOM -> showAlignBottom(view)
                BALLOON_CENTER -> showAtCenter(view)
                else -> showAlignStart(view)
            }
        }
    }

    private fun dismissAllBallons() {
        balloons.forEach {
            it.dismiss()
        }
    }

    enum class DialogType {
        SWIPE_COPY,
        SWIPE_UPDATE,
        SINGLE_COPY
    }

    companion object {
        const val SWIPE_MODE = "SWIPE_MODE"
        const val BALLOON_LEFT = 0
        const val BALLOON_TOP = 1
        const val BALLOON_BOTTOM = 2
        const val BALLOON_CENTER = 3
    }
}
