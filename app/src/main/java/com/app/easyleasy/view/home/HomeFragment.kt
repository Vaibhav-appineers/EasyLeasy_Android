package com.app.easyleasy.view.home

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.commonUtils.utility.extension.sharedPreference
import com.app.easyleasy.dagger.components.FragmentComponent
import com.app.easyleasy.databinding.FragmentHomeBinding
import com.app.easyleasy.mvvm.AppConfig
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.mvvm.BaseFragment
import com.app.easyleasy.viewModel.HomeViewModel

class HomeFragment : BaseFragment<HomeViewModel>() {

    private lateinit var binding: FragmentHomeBinding

    override fun setDataBindingLayout() {}

    override fun provideLayoutId(): Int {
        return R.layout.fragment_home
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
        //Referseh Native Adds
        if(!sharedPreference.isAdRemoved) {
            (activity as BaseActivity<*>).refreshAd(
                true,
                false,
                true,
                binding.adFrame
            )
        }
        addObservers()
    }

    private fun addObservers() {
        (activity?.application as AppineersApplication).isAdRemoved.observe(this@HomeFragment, Observer {
            if(it){
                binding.adView.visibility = View.GONE
                binding.adFrame.visibility = View.GONE
            }
        })
    }

}