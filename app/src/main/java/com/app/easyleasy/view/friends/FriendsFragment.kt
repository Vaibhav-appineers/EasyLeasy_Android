package com.app.easyleasy.view.friends

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.dagger.components.FragmentComponent
import com.app.easyleasy.databinding.FragmentFriendsBinding
import com.app.easyleasy.mvvm.AppConfig
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.mvvm.BaseFragment
import com.app.easyleasy.viewModel.FriendViewModel

class FriendsFragment : BaseFragment<FriendViewModel>() {

    private lateinit var binding: FragmentFriendsBinding

    override fun setDataBindingLayout() {}

    override fun provideLayoutId(): Int {
        return R.layout.fragment_friends
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
        addListeners()
        addObservers()
    }

    private fun addListeners() {
        binding.btnAdd.setOnClickListener {
            if (AppConfig.AdProvider_MoPub) {
                (activity as BaseActivity<*>).showInterstitial((activity as BaseActivity<*>))
            } else {
                (activity as BaseActivity<*>).showInterstitial()
            }
        }
    }

    private fun addObservers() {
        (activity?.application as AppineersApplication).isAdRemoved.observe(
            this@FriendsFragment,
            Observer {
                if (it) {
                    binding.adView.visibility = View.GONE
                }
            })
    }
}