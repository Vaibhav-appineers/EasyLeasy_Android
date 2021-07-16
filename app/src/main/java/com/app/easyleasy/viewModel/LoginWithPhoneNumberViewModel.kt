package com.app.easyleasy.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.getDeviceName
import com.app.easyleasy.commonUtils.utility.getDeviceOSVersion
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.LoginWithPhoneNumberRepository
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import kotlin.collections.HashMap

class LoginWithPhoneNumberViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val loginWithPhoneNumberRepository: LoginWithPhoneNumberRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    var deviceToken : String = ""

    override fun onCreate() {
        checkForInternetConnection()
        deviceToken = sharedPreference.deviceToken ?: ""
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var phoneNumberLoginMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()


    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    /**
     * Api call for login using phone number
     */
    fun callLoginWithPhoneNumber(
        phoneNumber: String,
        password: String
    ) {
        val map = HashMap<String, String>()
        map["mobile_number"] = phoneNumber
        map["password"] = password
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = deviceToken
        compositeDisposable.addAll(
            loginWithPhoneNumberRepository.callLoginWithPhoneNumber(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        //showDialog.postValue(false)
                        phoneNumberLoginMutableLiveData.postValue(response)
                    },
                    { error ->
                        //showDialog.postValue(false)
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    /**
     * Save logged in user information in shared preference
     */
    fun saveUserDetails(loginResponse: LoginResponse?) {
        //Logger.setUserInfo(loginResponse?.email ?: "")
        sharedPreference.isSkip = false
        sharedPreference.userDetail = loginResponse
        sharedPreference.isLogin = true
        sharedPreference.authToken = loginResponse?.accessToken ?: ""
        sharedPreference.isAdRemoved =  loginResponse!!.isAdsFree()
        sharedPreference.logStatusUpdated =
            loginResponse?.logStatusUpdated?.toLowerCase(Locale.getDefault()) ?: "inactive"
    }
}