package com.app.easyleasy.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.getDeviceName
import com.app.easyleasy.commonUtils.utility.getDeviceOSVersion
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.LoginWithPhoneNumberSocialRepository
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import kotlin.collections.HashMap

class LoginWithPhoneNumberSocialViewModel (
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val loginWithPhoneNumberSocialRepository: LoginWithPhoneNumberSocialRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    var deviceToken : String = ""

    override fun onCreate() {
        checkForInternetConnection()
        deviceToken = AppineersApplication.sharedPreference.deviceToken ?: ""
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var phoneNumberLoginMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    var loginSocialMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()

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
            loginWithPhoneNumberSocialRepository.callLoginWithPhoneNumber(map)
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
     * Api call for login using social account
     */
    fun callLoginWithSocial(socialType: String, socialId: String) {
        val map = HashMap<String, String>()
        map["social_login_type"] = socialType
        map["social_login_id"] = socialId
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = deviceToken
        compositeDisposable.addAll(
            loginWithPhoneNumberSocialRepository.callLoginWithPhoneNumberSocial(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        //showDialog.postValue(false)
                        loginSocialMutableLiveData.postValue(response)
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
        AppineersApplication.sharedPreference.isSkip = false
        AppineersApplication.sharedPreference.userDetail = loginResponse
        AppineersApplication.sharedPreference.isLogin = true
        AppineersApplication.sharedPreference.authToken = loginResponse?.accessToken ?: ""
        AppineersApplication.sharedPreference.isAdRemoved = loginResponse!!.isAdsFree()
        AppineersApplication.sharedPreference.logStatusUpdated =
            loginResponse?.logStatusUpdated?.toLowerCase(Locale.getDefault()) ?: "inactive"
    }
}