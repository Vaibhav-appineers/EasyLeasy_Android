package com.app.easyleasy.view

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.app.easyleasy.BuildConfig
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.dialog.AppUpdateDialog
import com.app.easyleasy.commonUtils.utility.extension.showSnackBar
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityHomeBinding
import com.app.easyleasy.dataclasses.VersionConfigResponse
import com.app.easyleasy.dataclasses.response.GoogleReceipt
import com.app.easyleasy.dataclasses.response.StaticPage
import com.app.easyleasy.mvvm.AppConfig
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.friends.FriendsFragment
import com.app.easyleasy.view.home.HomeFragment
import com.app.easyleasy.view.message.MessagesFragment
import com.app.easyleasy.view.profile.ProfileFragment
import com.app.easyleasy.view.settings.SettingsFragment
import com.app.easyleasy.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.easyleasy.viewModel.HomeViewModel
import com.google.gson.Gson
import com.hb.logger.Logger
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : BaseActivity<HomeViewModel>() {
    private lateinit var binding: ActivityHomeBinding

    companion object {
        const val TAG = "HomeActivity"
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-homeScreen", "view_homeScreen", "view_homeScreen")
        binding.apply {
            bottomNavigation.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.action_home -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Home Button CLick")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is HomeFragment) {
                            true
                        } else {
                            setCurrentFragment(HomeFragment())
                            true
                        }
                    }
                    R.id.action_friends -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Friends Tab CLick")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is FriendsFragment) {
                            true
                        } else {
                            setCurrentFragment(FriendsFragment())
                            true
                        }
                    }
                    R.id.action_message -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Message Tab Click")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is MessagesFragment) {
                            true
                        } else {
                            setCurrentFragment(MessagesFragment())
                            true
                        }
                    }
                    R.id.action_profile -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "My Profile Tab Click")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is ProfileFragment) {
                            true
                        } else {
                            setCurrentFragment(ProfileFragment())
                            true
                        }
                    }
                    R.id.action_settings -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Settings Tab Click")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is SettingsFragment) {
                            true
                        } else {
                            setCurrentFragment(SettingsFragment())
                            true
                        }
                    }
                    else -> false
                }
            }
        }

        addObservers()
        when {
            checkInternet() -> viewModel.callGetConfigParameters()
        }

        binding.bottomNavigation.selectedItemId = R.id.action_home
    }

    /**
     * Show dialog for new application version is available
     * @param version VersionConfigResponse
     */
    private fun showNewVersionAvailableDialog(version: VersionConfigResponse) {
        AppUpdateDialog(this, version).show(supportFragmentManager, "update dialog")
    }

    private fun addObservers() {
        viewModel.configParamsPhoneLiveData.observe(this, Observer {
            if (it.settings?.isSuccess == true) {
                updateUserSubscriptionData(it.data!!.get(0))
                (application as AppineersApplication).isAdRemoved.value = it.data!![0].isAdsFree()
                sharedPreference.isAdRemoved = it.data!![0].isAdsFree()
                updateAdConfiguration(it.data!![0])
                Log.i(TAG, "addObserver: " + it.data!![0].isAdsFree())
                if (!it.data.isNullOrEmpty()) {
                    val config = it.data!![0]
                    if (config.shouldShowVersionDialog(this@HomeActivity)) {
                        showNewVersionAvailableDialog(config)
                    }
                    var pageCodeList: ArrayList<StaticPage> = ArrayList()
                    if (config.shouldShowTNCUpdated()) {
                        pageCodeList.add(
                            StaticPage(
                                pageCode = IConstants.STATIC_PAGE_TERMS_CONDITION,
                                forceUpdate = true
                            )
                        )
                    }
                    if (config.shouldShowPrivacyPolicyUpdated()) {
                        pageCodeList.add(
                            StaticPage(
                                pageCode = IConstants.STATIC_PAGE_PRIVACY_POLICY,
                                forceUpdate = true
                            )
                        )
                    }
                    if (!pageCodeList.isNullOrEmpty()) {
                        startActivity(
                            StaticPagesMultipleActivity.getStartIntent(
                                mContext = this@HomeActivity,
                                pageCodeList = pageCodeList
                            )
                        )
                    }


                    if (!BuildConfig.DEBUG) {
                        Log.e(
                            "LOGGING_API_CALL",
                            config.logStatusUpdated.toLowerCase(Locale.getDefault())
                        )
                        (application as AppineersApplication).isLogStatusUpdated.value =
                            (config.logStatusUpdated.toLowerCase(
                                Locale.getDefault()
                            )) == "active"
                        sharedPreference.logStatusUpdated =
                            config.logStatusUpdated.toLowerCase(Locale.getDefault())
                        if (sharedPreference.logStatusUpdated.equals("inactive", true)) {
                            Logger.clearAllLogs()
                            Logger.disableLogger()
                        } else if (sharedPreference.logStatusUpdated.equals("active", true)) {
                            Logger.enableLogger()
                        }
                    }
                    else{
                        Logger.enableLogger()
                    }
                }
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@HomeActivity)
            }
        })

        viewModel.orderReceiptJsonForSubscription.observe(this@HomeActivity, Observer {
            if (it.isNotEmpty()) {

                val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")

                viewModel.callBuySubscription(receiptData)

            }
        })
        viewModel.buySubscriptionLiveData.observe(this@HomeActivity, Observer {
            if (it.settings?.isSuccess == true) {
                val userDetails = sharedPreference.userDetail
                userDetails?.subscription = it.data?.get(0)!!.subscription
                sharedPreference.userDetail = userDetails
                // val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                // logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")
                // viewModel.callBuySubscription(receiptData)
                //  navigateToHomeScreen()
            } else {
                it.settings?.message?.showSnackBar(this@HomeActivity, IConstants.SNAKBAR_TYPE_ERROR, duration = IConstants.SNAKE_BAR_SHOW_TIME_INT)
                // mIdlingResource?.setIdleState(true)
            }
        })
    }


    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameContainer, fragment)
            commit()
        }


    /** Update AdConfiguration based on config api response*/
    private fun updateAdConfiguration(data: VersionConfigResponse?) {
        if (data != null) {
            sharedPreference.projectDebugLevel = data.projectDebugLevel
            sharedPreference.androidBannerId = data.androidBannerId
            sharedPreference.androidInterstitialId = data.androidInterstitialId
            sharedPreference.androidNativeId = data.androidNativeId
            sharedPreference.androidRewardedId = data.androidRewardedId
            sharedPreference.androidMoPubBannerId = data.androidMoPubBannerId
            sharedPreference.androidMopubInterstitialId = data.androidMopubInterstitialId

            if (AppConfig.AdProvider_ADMob) {
                (application as AppineersApplication).initGoogleAdMobSDK()

            } else if (AppConfig.AdProvider_MoPub) {
                (application as AppineersApplication).initMoPubSDK(data.isAppInDevelopment())
            }

            val adConfig = StringBuilder()
            adConfig.append("projectDebugLevel= " + data.projectDebugLevel)
            adConfig.append(", androidBannerId= " + data.androidBannerId)
            adConfig.append(", androidInterstitialId= " + data.androidInterstitialId)
            adConfig.append(", androidNativeId= " + data.androidNativeId)
            adConfig.append(", androidRewardedId= " + data.androidRewardedId)
            adConfig.append(", androidMoPubBannerId= " + data.androidMoPubBannerId)
            adConfig.append(", androidMopubInterstitialId= " + data.androidMopubInterstitialId)
            Log.i(TAG, "adConfig Info: " + adConfig.toString())
            logger.dumpCustomEvent("adConfig Info", adConfig.toString())
        }
    }
    private fun updateUserSubscriptionData(data: VersionConfigResponse?) {
        if (data != null) {
            val userDetails = sharedPreference.userDetail
            if (userDetails != null) {
                if( data.subscription?.size!!>=0){
                    userDetails.subscription=data.subscription
                    sharedPreference.userDetail = userDetails
                }
            }
        }
    }
}