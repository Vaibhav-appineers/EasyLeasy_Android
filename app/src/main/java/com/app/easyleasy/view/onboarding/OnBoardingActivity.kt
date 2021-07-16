package com.app.easyleasy.view.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.commonUtils.common.DepthPageTransformer
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.extension.sharedPreference
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityOnBoardingBinding
import com.app.easyleasy.dataclasses.onBoardings
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.viewModel.OnBoardingActivityViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_on_boarding.*

class OnBoardingActivity : BaseActivity<OnBoardingActivityViewModel>() {

    private lateinit var onBoardingAdapter: OnBoardingAdapter
    private var binding: ActivityOnBoardingBinding? = null
    private var currentPosition: Int = 0

    private var viewpagerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            currentPosition = position
            when (currentPosition) {
                0 -> mbtnPrevious.isEnabled = false
                2 -> mbtnNext.text = getString(R.string.label_start_button)
                else -> {
                    mbtnPrevious.isEnabled = true
                    mbtnNext.text = getString(R.string.label_next_button)
                }
            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_boarding)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-onBoardingScreen","view_onBoardingScreen","view_onBoardingScreen")
        binding?.let {
            with(it, {
                setOnBoardingDetails(binding!!.vpOnBoarding)
                setTabLayout(binding!!.mTabLayout, binding!!.vpOnBoarding)

                binding?.mTabLayout?.touchables?.forEach { tabDots ->
                    tabDots.isEnabled = false
                }

                binding?.mbtnPrevious?.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK,"Previous Button Click")
                    currentPosition--
                    vpOnBoarding.currentItem = currentPosition
                }

                binding?.mbtnNext?.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK,"Next Button Click")
                    currentPosition.let { position ->
                        when (position) {
                            2 -> {
                                sharedPreference.isOnBoardingShown = true
                                openLoginActivity()
                            }
                            else -> {
                                currentPosition++
                                vpOnBoarding.currentItem = currentPosition
                            }
                        }
                    }
                }

                binding?.mbtnSkip?.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK,"Skip Button Click")
                    openLoginActivity()
                }
            })
        }
    }

    private fun setTabLayout(mTabLayout: TabLayout, vpOnBoarding: ViewPager2) {
        TabLayoutMediator(mTabLayout,
            vpOnBoarding) { _, _ ->
        }.attach()
    }

    private fun setOnBoardingDetails(vpOnBoarding: ViewPager2) {
        onBoardingAdapter = OnBoardingAdapter(onBoardings)
        vpOnBoarding.adapter = onBoardingAdapter
        vpOnBoarding.registerOnPageChangeCallback(viewpagerPageChangeCallback)
        vpOnBoarding.setPageTransformer(DepthPageTransformer())
    }

    private fun openLoginActivity() {
        startActivity(
            Intent(
                this@OnBoardingActivity,
                (application as AppineersApplication).getLoginActivity()
            )
        )
        finish()
    }
}