package com.app.easyleasy.viewModel

import android.app.Application
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.utility.validation.*
import com.app.easyleasy.R
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.api.network.WebServiceUtils
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.commonUtils.common.*
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.commonUtils.utility.PasswordStrength
import com.app.easyleasy.commonUtils.utility.extension.*
import com.app.easyleasy.commonUtils.utility.validation.createValidationResult
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.request.SignUpRequestModel
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.app.easyleasy.dataclasses.response.OTPResponse
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.SignUpRepository
import com.app.easyleasy.view.authentication.signup.signupconfig.SignUpConfigItem
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import kotlin.collections.HashMap

class SignUpViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val application: Application,
    private val signUpRepository: SignUpRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    override fun onCreate() {
        checkForInternetConnection()
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var otpLiveData = MutableLiveData<TAListResponse<OTPResponse>>()
    var signUpLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    var signUpLiveDataSocial = MutableLiveData<TAListResponse<LoginResponse>>()
    val signUpRequestModel = SignUpRequestModel()

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun callSignUpWithEmail() {
        compositeDisposable.addAll(
            signUpRepository.callSignUpWithEmail(
                signUpRequestModel.toFieldRequestBodyMap(),
                file = if (signUpRequestModel.profileImage.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart(
                    "user_profile",
                    signUpRequestModel.profileImage
                )
            )
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        //showDialog.postValue(false)
                        signUpLiveData.postValue(response)
                    },
                    { error ->
                        //showDialog.postValue(false)
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    fun callSignUpWithSocial() {
        compositeDisposable.addAll(
            signUpRepository.callSignUpWithSocial(
                signUpRequestModel.toFieldRequestBodyMap(),
                file = if (signUpRequestModel.profileImage.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart(
                    "user_profile",
                    signUpRequestModel.profileImage
                )
            )
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        //showDialog.postValue(false)
                        signUpLiveDataSocial.postValue(response)
                    },
                    { error ->
                        //showDialog.postValue(false)
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    fun callCheckUnique(type: String, email: String, phone: String, userName: String) {
        val map = HashMap<String, String>()
        map["type"] = type // phone/email
        map["email"] = email
        map["mobile_number"] = PhoneNumberUtils.normalizeNumber(phone)
        map["user_name"] = userName
        compositeDisposable.addAll(
            signUpRepository.callCheckUniqueUser(map = map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        //showDialog.postValue(false)
                        otpLiveData.postValue(response)
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
        AppineersApplication.sharedPreference.isSkip = false
        AppineersApplication.sharedPreference.userDetail = loginResponse
        AppineersApplication.sharedPreference.isLogin = true
        AppineersApplication.sharedPreference.authToken = loginResponse?.accessToken ?: ""
        AppineersApplication.sharedPreference.isAdRemoved = loginResponse!!.isAdsFree()
        AppineersApplication.sharedPreference.logStatusUpdated =
            loginResponse?.logStatusUpdated?.toLowerCase(
                Locale.getDefault()
            ) ?: "inactive"
    }

    /**
     * Validate Signup request
     * @param request SignUpRequestModel
     * @return Boolean Return true if all required fields are valid
     */
    fun isValid(request: SignUpRequestModel): Boolean {
        return when {

            //User Name
            !isValidInputText(
                text = request.userName, emptyFailType = USER_NAME_EMPTY,
                invalidFailType = USER_NAME_INVALID, viewId = R.id.tietUserName,
                minimumLength = application.resources.getInteger(R.integer.user_name_min_length),
                maximumLength = application.resources.getInteger(R.integer.user_name_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "1")
            ) -> {
                false
            }

            //First Name
            !isValidInputText(
                text = request.firstName, emptyFailType = FIRST_NAME_EMPTY,
                invalidFailType = FIRST_NAME_INVALID, viewId = R.id.tietFirstName,
                minimumLength = application.resources.getInteger(R.integer.first_name_min_length),
                maximumLength = application.resources.getInteger(R.integer.first_name_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }


            //Last Name
            !isValidInputText(
                text = request.lastName, emptyFailType = LAST_NAME_EMPTY,
                invalidFailType = LAST_NAME_INVALID, viewId = R.id.tietLastName,
                minimumLength = application.resources.getInteger(R.integer.first_name_min_length),
                maximumLength = application.resources.getInteger(R.integer.first_name_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }


            //Email
            !isValidInputEmail(
                email = request.email,
                validationObserver = validationObserver
            ) -> {
                false
            }

            //Phone number
            !isValidInputPhone(
                phoneNumber = request.mobileNumber,
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }

            //Street Address
            !isValidInputText(
                text = request.address, emptyFailType = ADDRESS_EMPTY, viewId = R.id.tietAddress,
                validationObserver = validationObserver, config = SignUpConfigItem("1", "1")
            ) -> {
                false
            }

            //City
            !isValidInputText(
                text = request.city, emptyFailType = CITY_EMPTY, viewId = R.id.tietCity,
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }

            //State
            !isValidInputText(
                text = request.state, emptyFailType = STATE_EMPTY, viewId = R.id.tietState,
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }

            //Zip Code
            !isValidInputText(
                text = request.zipCode, emptyFailType = ZIP_CODE_EMPTY,
                invalidFailType = ZIP_CODE_INVALID, viewId = R.id.tietZipCode,
                minimumLength = application.resources.getInteger(R.integer.zip_code_min_length),
                maximumLength = application.resources.getInteger(R.integer.zip_code_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }

            //DOB
            !isValidInputText(
                text = request.dob, emptyFailType = DOB_EMPTY, viewId = R.id.tietDOB,
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }

            request.socialType.isEmpty() && request.password.isEmpty() -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_EMPTY, R.id.tietPassword)
                false
            }
            request.socialType.isEmpty() && PasswordStrength.calculateStrength(request.password).value < PasswordStrength.STRONG.value -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_INVALID, R.id.tietPassword)
                false
            }
            request.socialType.isEmpty() && request.confirmPassword.isEmpty() -> {
                validationObserver.value =
                    createValidationResult(CONFORM_PASSWORD_EMPTY, R.id.tietConfirmPassword)
                false
            }
            request.socialType.isEmpty() && request.password != request.confirmPassword -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_NOT_MATCH, R.id.tietConfirmPassword)
                false
            }

            !request.tnc -> {
                validationObserver.value =
                    createValidationResult(TNC_NOT_ACCEPTED, R.id.cbTermsAndPolicy)
                false
            }

            else -> true
        }
    }
}