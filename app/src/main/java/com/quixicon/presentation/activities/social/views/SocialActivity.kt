package com.quixicon.presentation.activities.social.views

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quixicon.BR
import com.quixicon.R
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.social.SocialEvent
import com.quixicon.core.support.extensions.ActivityExtensions.setStatusBarColor
import com.quixicon.core.support.extensions.changeAppearance
import com.quixicon.core.support.extensions.error
import com.quixicon.core.support.extensions.getViewModel
import com.quixicon.core.support.extensions.initToolbar
import com.quixicon.core.support.extensions.progress
import com.quixicon.core.support.extensions.success
import com.quixicon.core.support.helpers.SupportHelper
import com.quixicon.core.system.EventArgs
import com.quixicon.databinding.ActivitySocialBinding
import com.quixicon.databinding.RowSocialNetworkBinding
import com.quixicon.domain.entities.enums.SocialType
import com.quixicon.domain.exceptions.Failure
import com.quixicon.domain.exceptions.FailureException
import com.quixicon.presentation.activities.base.DaggerInternetActivity
import com.quixicon.presentation.activities.social.adapters.SocialNetworksAdapter
import com.quixicon.presentation.activities.social.models.SocialNetworkModel
import com.quixicon.presentation.activities.social.viewmodels.SocialViewModel
import com.quixicon.presentation.views.AsyncOperationView
import timber.log.Timber
import javax.inject.Inject

class SocialActivity :
    DaggerInternetActivity(),
    AsyncOperationView,
    SocialView,
    SocialNetworksAdapter.OnItemClickListener {

    private lateinit var binding: ActivitySocialBinding

    private lateinit var viewModel: SocialViewModel

    private lateinit var adapter: SocialNetworksAdapter

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_social)
        initComponents()
        getRemoteSocialNetworks()
        Metrics.send(this, SocialEvent.SHOW)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
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

    override fun onBindSocialNetworks(args: EventArgs<List<SocialNetworkModel>>) {
        val items = args.content
        Timber.e("Binding ${items.size} items")
        adapter.update(items)
    }

    override fun onItemClick(variable: SocialNetworkModel?, binding: RowSocialNetworkBinding?) {
        variable?.run {
            val socialIntent = when (type) {
                SocialType.TELEGRAM -> {
                    Metrics.send(baseContext, SocialEvent.TELEGRAM)
                    SupportHelper.getTelegramIntent(baseContext, url)
                }
                SocialType.VK -> {
                    Metrics.send(baseContext, SocialEvent.VK)
                    SupportHelper.getVKIntent(baseContext, url)
                }
                SocialType.FACEBOOK -> {
                    Metrics.send(baseContext, SocialEvent.FACEBOOK)
                    SupportHelper.getFacebookIntent(url)
                }
                SocialType.INSTAGRAM -> {
                    Metrics.send(baseContext, SocialEvent.INSTAGRAM)
                    SupportHelper.getInstagramIntent(baseContext, url)
                }
                SocialType.TWITTER -> {
                    Metrics.send(baseContext, SocialEvent.TWITTER)
                    SupportHelper.getTwitterIntent(url)
                }
            }
            Timber.e("You clicked to $type:$url")
            startActivity(socialIntent)
        }
    }

    private fun initComponents() {
        initViewModel()

        with(binding) {
            executePendingBindings()
            invalidateAll()
        }
        setStatusBarColor(R.color.transparent)
        initToolbar(R.id.toolbar, R.string.menu_follow)
        initAdapter()
    }

    private fun initViewModel() {
        viewModel = getViewModel(vmFactory) {
            progress(progressLiveData, ::onAsyncOperationProgress)
            error(errorLiveData, ::onAsyncOperationError)
            success(getSocialNetworksLiveData, ::onBindSocialNetworks)
        }
    }

    private fun initAdapter() {
        adapter = SocialNetworksAdapter(
            this,
            arrayListOf(),
            BR.model,
            R.layout.row_social_network,
            BR.onClickListener,
            this
        )

        binding.recyclerView.let {
            it.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            it.adapter = adapter
        }
    }

    private fun getRemoteSocialNetworks() {
        viewModel.getRemoteSocialNetworks()
    }
}
