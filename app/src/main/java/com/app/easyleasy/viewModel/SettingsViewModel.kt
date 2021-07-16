package com.app.easyleasy.viewModel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.SkuDetails
import com.app.easyleasy.BuildConfig
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.GoogleReceipt
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.app.easyleasy.mvvm.AppConfig
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.SettingsRepository
import com.app.easyleasy.mvvm.SettingViewConfig
import com.app.easyleasy.repository.inappbilling.BillingRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class SettingsViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val billingRepository: BillingRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    var subscriptionSKUList = MutableLiveData<List<SkuDetails>>()
    val logoutLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val deleteAccountLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var goAdFreeLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    var addFreeSKU = MutableLiveData<SkuDetails>()
    var orderReceiptJson = MutableLiveData<String>()
    var settingConfig: SettingViewConfig
    var inAppSKUList = MutableLiveData<List<SkuDetails>>()

    var orderReceiptJsonForOneTime = MutableLiveData<String>()
    var orderReceiptJsonForSubscription = MutableLiveData<String>()
    var orderReceiptJsonForUpgradeDowngradeSubscription = MutableLiveData<String>()
    var buySubscriptionLiveData = MutableLiveData<TAListResponse<LoginResponse>>()



    override fun onCreate() {
        checkForInternetConnection()
    }

    init {
        billingRepository.startDataSourceConnections()
        addFreeSKU = billingRepository.addFreeSKU
        orderReceiptJson = billingRepository.orderReceiptJson
        subscriptionSKUList = billingRepository.subscriptionSKUList
        inAppSKUList = billingRepository.inAppSKUList
        orderReceiptJsonForOneTime = billingRepository.orderReceiptJsonForOneTime
        orderReceiptJsonForSubscription = billingRepository.orderReceiptJsonForSubscription
        orderReceiptJsonForUpgradeDowngradeSubscription = billingRepository.orderReceiptJsonForUpgradeDowngradeSubscription
        settingConfig = setUpSettingConfig()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun callLogout() {
        compositeDisposable.addAll(
            settingsRepository.callLogout()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        logoutLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    fun callDeleteAccount() {
        compositeDisposable.addAll(
            settingsRepository.callDeleteAccount()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        deleteAccountLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    /**
     * Code to set up all settings config of user
     */
    private fun setUpSettingConfig(): SettingViewConfig {
        val user = sharedPreference.userDetail
        return SettingViewConfig().apply {
            showNotification = !sharedPreference.isSkip
            showRemoveAdd =
                ((AppConfig.BANNER_AD || AppConfig.INTERSTITIAL_AD) && !sharedPreference.isAdRemoved && !sharedPreference.isSkip)
            showEditProfile = !sharedPreference.isSkip
            showChangePassword = !(user?.isSocialLogin() == true || sharedPreference.isSkip)
            showChangePhone = ((AppineersApplication()).getApplicationLoginType()
                .equals(IConstants.LOGIN_TYPE_PHONE, true) ||
                    (AppineersApplication()).getApplicationLoginType().equals(
                        IConstants.LOGIN_TYPE_PHONE_SOCIAL,
                        true
                    )) && !sharedPreference.isSkip
            showDeleteAccount = !sharedPreference.isSkip
            showSendFeedback = !sharedPreference.isSkip
            showLogOut = !sharedPreference.isSkip
            appVersion = "Version: " + BuildConfig.VERSION_NAME
        }
    }

    fun makePurchase(activity: Activity?, skuDetails: SkuDetails?,purchaseToken:String="",oldSku:String="" ) {

        if (activity != null && skuDetails != null&&purchaseToken.isEmpty())
            billingRepository.launchBillingFlow(activity, skuDetails = skuDetails)
        if (activity != null && skuDetails != null&&purchaseToken.isNotEmpty())
            billingRepository.launchBillingFlowUpgrading(activity, skuDetails = skuDetails,purchaseToken = purchaseToken,oldSku = oldSku)

    }



    fun callGoAdFree(receiptData: String) {
        val map = HashMap<String, String>()
        map["one_time_transaction_data"] = receiptData
        compositeDisposable.addAll(
            settingsRepository.callGoAdFree(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        goAdFreeLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    /**
     * Clear all user saved data
     */
    fun performLogout() {
        sharedPreference.userDetail = null
        sharedPreference.isLogin = false
        sharedPreference.authToken = ""
    }

    /**
     * Api call for send transaction data on server
     * @param receiptData In-App purchase receipt data
     */


    fun callBuySubscription(receiptData: GoogleReceipt?) {
        val map = HashMap<String, String>()
        if (receiptData != null) {

            map["subscription_id"] = receiptData.productId
            map["receipt_type"] = receiptData.receiptType
            map["purchase_token"] = receiptData.purchaseToken
            map["PACKAGE_NAME"] = receiptData.packageName
        }
        compositeDisposable.addAll(
            settingsRepository.callBuySubscription(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        buySubscriptionLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }


}