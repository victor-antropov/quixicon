package com.quixicon.presentation.fragments.cardaction.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quixicon.R
import com.quixicon.core.support.extensions.ViewExtensions.gone
import com.quixicon.presentation.activities.cards.entities.CardAction
import com.quixicon.presentation.fragments.cardaction.adapters.ActionAdapter

class CardActionDialogFragment : DialogFragment() {

    private var selectedItem = 0
    private var itemList = ArrayList<CardAction>()

    lateinit var actionAdapter: ActionAdapter

    var created = false

    var titleId: Int = 0
    var subtitleId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        created = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        actionAdapter = ActionAdapter(requireContext(), R.layout.row_item_radio, itemList.map { it.name })

        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.fragment_card_action_dialog, null)

        with(view) {
            findViewById<TextView>(R.id.tv_dialog_title).text = getString(titleId)

            findViewById<TextView>(R.id.tv_subtitle).run {
                if (subtitleId > 0) {
                    text = getString(subtitleId)
                } else {
                    gone()
                }
            }

            findViewById<ListView>(R.id.list_view)?.run {
                adapter = actionAdapter
                setOnItemClickListener { _, _, position, _ ->
                    actionAdapter.selected = position
                }
            }

            itemList.getOrNull(selectedItem)?.run {
                actionAdapter.selected = selectedItem
            }

            findViewById<View>(R.id.btn_save).setOnClickListener {
                val action = itemList[actionAdapter.selected]
                (activity as? Listener)?.onCardActionDialogConfirm(action)
                dismiss()
            }

            findViewById<View>(R.id.btn_close).setOnClickListener {
                dismiss()
            }
        }

        return MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Quixicon_MaterialDialog)
            .setView(view)
            .setCancelable(false)
            .create().apply {
                window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        created = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    /**
     * Restores properties from the bundle instance.
     *
     * @param savedInstanceState Saved state of current fragment dialog.
     */
    private fun restoreProperties(savedInstanceState: Bundle) {
        with(savedInstanceState) {
            titleId = getInt(TITLE)
            subtitleId = getInt(SUBTITLE)
            selectedItem = getInt(SELECTED_ITEM)
            itemList = getParcelableArrayList(ITEMS_LIST) ?: arrayListOf()
        }
    }

    /**
     * Restores state of fragment dialog.
     *
     * @param savedInstanceState Saved state of current fragment dialog.
     */
    protected fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also { restoreProperties(it) } ?: run {
            arguments?.also { restoreProperties(it) }
        }
    }

    /**
     * Saves current state of fragment dialog.
     *
     * @param outState Parameter for store current fragment dialog state.
     */
    protected fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putInt(TITLE, titleId)
            putInt(SUBTITLE, subtitleId)
            putParcelableArrayList(ITEMS_LIST, itemList)
            putInt(SELECTED_ITEM, selectedItem)
        }
    }

    companion object {
        const val TITLE = "TITLE"
        const val SUBTITLE = "SUBTITLE"
        const val ITEMS_LIST = "ITEM_LIST"
        const val SELECTED_ITEM = "SELECTED_ITEM"

        @JvmStatic
        fun createInstance(
            @StringRes titleId: Int,
            @StringRes subtitleId: Int? = null,
            items: List<CardAction>,
            selected: Int,
        ) = CardActionDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(TITLE, titleId)
                putInt(SUBTITLE, subtitleId ?: 0)
                putParcelableArrayList(ITEMS_LIST, ArrayList(items))
                putInt(SELECTED_ITEM, selected)
            }
        }
    }

    interface Listener {
        fun onCardActionDialogConfirm(action: CardAction)
    }
}
