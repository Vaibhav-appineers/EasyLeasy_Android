package com.app.easyleasy.viewModel

import android.app.Application
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.R
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.common.timeToMinuteSecond
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.dataclasses.createValidationResult
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.forgotpasswordwithphone.ResetWithPhone
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.mvvm.utility.OTP_EMPTY
import com.app.easyleasy.mvvm.utility.OTP_INVALID
import com.app.easyleasy.repository.OTPForgotPasswordRepository
import io.reactivex.disposables.CompositeDisposable

class OTPForgotPasswordViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val otpForgotPasswordRepository: OTPForgotPasswordRepository,
    val app: Application
): BaseViewModel(schedulerProvider,compositeDisposable,networkHelper) {

    val timeLiveData = MutableLiveData<String>()
    val enableRetryLiveData = MutableLiveData<Boolean>()
    var countDownTimer: CountDownTimer? = null
    val resendOtpLiveData = MutableLiveData<TAListResponse<ResetWithPhone>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() = when {
        checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
        else -> checkForInternetConnectionLiveData.postValue(false)
    }

    fun getTimerValue(): MutableLiveData<String> {
        return timeLiveData
    }

    fun getEnableRetrySettings(): MutableLiveData<Boolean> {
        return enableRetryLiveData
    }

    fun startTimer() {
        countDownTimer = getCountTimer()
        countDownTimer?.start()
        Log.i("TAG", "startTimer: "+getCountTimer())
    }

    fun cancelTimer(markFinish: Boolean = false) {
        countDownTimer?.cancel()
        if (markFinish)
            countDownTimer?.onFinish()
    }

    fun isValid( otp: String , sendOtp: String): Boolean {
        return  when {
            otp.isEmpty() -> {
                validationObserver.value = createValidationResult(failType = OTP_EMPTY)
                false
            }
            otp != sendOtp -> {
                validationObserver.value = createValidationResult(failType = OTP_INVALID)
                false
            }
            else-> true
        }
    }
    /**
     * count down timer function
     */
    fun getCountTimer(): CountDownTimer? {
        return object : CountDownTimer(30000L, 1000) {
            override fun onFinish() {
                timeLiveData.value = ""
                enableRetryLiveData.value = true
            }

            override fun onTick(millisUntilFinished: Long) {
                timeLiveData.value = String.format(
                    app.getString(
                        R.string.lbl_resend_otp_in_minute,
                        millisUntilFinished.timeToMinuteSecond()
                    )
                )
            }
        }
    }

    fun getOTPForgotPasswordWithPhone(mobileNumber: String) {
        compositeDisposable.addAll(
            otpForgotPasswordRepository.getOTPForgotPasswordPhoneResponse(mobileNumber)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        resendOtpLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))

                    }
                )
        )

    }

}