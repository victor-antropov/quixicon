package com.quixicon.presentation.activities.help.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.quixicon.BR
import com.quixicon.R
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.databinding.ActivityHelpBinding
import com.quixicon.domain.entities.ExtraKey
import com.quixicon.presentation.activities.help.adapters.HelpAdapter
import com.quixicon.presentation.activities.help.models.HelpPageModel
import timber.log.Timber

class HelpActivity : AppCompatActivity(), HelpAdapter.Listener {

    private lateinit var binding: ActivityHelpBinding

    private lateinit var adapter: HelpAdapter

    private var startPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_help)
        initComponents()
        Timber.e("Activity Help created")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onFlipBack() {
        val position = binding.viewPager.currentItem - 1
        if (position >= 0) {
            binding.viewPager.setCurrentItem(position, true)
        }
    }

    override fun onFlipForward() {
        val size = binding.viewPager.adapter?.itemCount ?: 0
        val position = binding.viewPager.currentItem + 1

        if (position < size) {
            binding.viewPager.setCurrentItem(position, true)
        }
    }

    private fun initComponents() {
        with(binding) {
            executePendingBindings()
            invalidateAll()
        }
        setStatusBarColor(R.color.transparent)
        initToolbar(R.id.toolbar, R.string.menu_help)
        initAdapter()
    }

    private fun initAdapter() {
        val items = listOf(
            HelpPageModel(getString(R.string.help_brief), 5, 1),
            HelpPageModel(getString(R.string.help_card), 5, 2),
            HelpPageModel(getString(R.string.help_main), 5, 3),
            HelpPageModel(getString(R.string.help_collection), 5, 4),
            HelpPageModel(getString(R.string.help_test), 5, 5)
        )

        adapter = HelpAdapter(
            this,
            items,
            BR.model,
            R.layout.row_help_page,
            BR.onClickListener,
            this
        )

        binding.viewPager.let {
            it.adapter = adapter
            it.currentItem = 0
        }
    }

    private fun saveInstanceState(outState: Bundle) {
        outState.apply {
            putInt(ExtraKey.POSITION.name, startPosition)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            startPosition = it.getInt(ExtraKey.POSITION.name, 0)
        } ?: run {
            intent?.also {
                startPosition = it.getIntExtra(ExtraKey.POSITION.name, 0)
            }
        }
    }
}
