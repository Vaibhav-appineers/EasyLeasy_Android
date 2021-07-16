package com.app.easyleasy.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.dataclasses.VersionConfigResponse
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.GoogleReceipt
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.HomeRepository
import com.app.easyleasy.repository.inappbilling.BillingRepository
import io.reactivex.disposables.CompositeDisposable

class HomeViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val billingRepository: BillingRepository,
    private val homeRepository: HomeRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val configParamsPhoneLiveData = MutableLiveData<TAListResponse<VersionConfigResponse>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()

    var orderReceiptJsonForOneTime = MutableLiveData<String>()
    var orderReceiptJsonForSubscription = MutableLiveData<String>()
    var orderReceiptJsonForUpgradeDowngradeSubscription = MutableLiveData<String>()
    var buySubscriptionLiveData = MutableLiveData<TAListResponse<LoginResponse>>()

    override fun onCreate() {
        checkForInternetConnection()
    }
    init {

        orderReceiptJsonForOneTime = billingRepository.orderReceiptJsonForOneTime
        orderReceiptJsonForSubscription = billingRepository.orderReceiptJsonForSubscription
        orderReceiptJsonForUpgradeDowngradeSubscription = billingRepository.orderReceiptJsonForUpgradeDowngradeSubscription

    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    /**
     * Api call for getting config parameters
     */
    fun callGetConfigParameters() {
        compositeDisposable.addAll(
            homeRepository.callConfigParameters()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        configParamsPhoneLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
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
            homeRepository.callBuySubscription(map)
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