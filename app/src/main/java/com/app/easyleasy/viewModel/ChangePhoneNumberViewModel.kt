package com.app.easyleasy.viewModel

import android.telephony.PhoneNumberUtils
import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.utility.validation.*
import com.app.easyleasy.R
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.common.isValidMobileNumber
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.commonUtils.utility.extension.isValidMobileLenght
import com.app.easyleasy.dataclasses.createValidationResult
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.OTPResponse
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.ChangePhoneNumberRepository
import io.reactivex.disposables.CompositeDisposable

class ChangePhoneNumberViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val changePhoneNumberRepository: ChangePhoneNumberRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val changePhoneNumberLiveData = MutableLiveData<TAListResponse<OTPResponse>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    /**
     * Api call for verifing if user is unique
     */
    fun checkUniqueUser(
        type: String,
        email: String = "",
        phone: String = "",
        userName: String = ""
    ) {
        val map = HashMap<String, String>()
        map["type"] = type // phone/email
        map["email"] = email
        map["mobile_number"] = PhoneNumberUtils.normalizeNumber(phone)
        map["user_name"] = userName

        compositeDisposable.addAll(
            changePhoneNumberRepository.checkUniqueUser(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        changePhoneNumberLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    /**
     * Validate inputs
     */
    fun isValid(phoneNumber: String): Boolean {
        return when {

            phoneNumber.isEmpty() -> {
                validationObserver.value =
                    createValidationResult(PHONE_NUMBER_EMPTY, R.id.tietNewPhoneNumber)
                false
            }

            !phoneNumber.isValidMobileNumber() -> {
                validationObserver.value =
                    createValidationResult(PHONE_NUMBER_INVALID, R.id.tietNewPhoneNumber)
                return false
            }

            !phoneNumber.isValidMobileLenght() -> {
                validationObserver.value = createValidationResult(
                    PHONE_NUMBER_INVALID_LENGHT, R.id.tietNewPhoneNumber
                )
                return false
            }
            else -> true
        }
    }
}