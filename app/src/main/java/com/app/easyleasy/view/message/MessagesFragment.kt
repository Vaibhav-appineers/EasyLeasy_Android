package com.app.easyleasy.view.message

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.dagger.components.FragmentComponent
import com.app.easyleasy.databinding.FragmentMessagesBinding
import com.app.easyleasy.mvvm.AppConfig
import com.app.easyleasy.mvvm.BaseFragment
import com.app.easyleasy.viewModel.MessageViewModel

class MessagesFragment : BaseFragment<MessageViewModel>() {

    private lateinit var binding: FragmentMessagesBinding

    override fun setDataBindingLayout() {}

    override fun provideLayoutId(): Int {
        return R.layout.fragment_messages
    }

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupView(view: View) {
        binding = DataBindingUtil.bind(view)!!
        binding.lifecycleOwner = this
        if (AppConfig.AdProvider_MoPub) {
            this.activity?.let { showBannerAd(it, binding.moPubAdView) }
        } else {
            this.activity?.let { showBannerAd(it, binding.adView) }
        }

        addObservers()
    }

    private fun addObservers() {
        (activity?.application as AppineersApplication).isAdRemoved.observe(this@MessagesFragment, Observer {
            if(it){
                binding.adView.visibility = View.GONE
            }
        })
    }
}