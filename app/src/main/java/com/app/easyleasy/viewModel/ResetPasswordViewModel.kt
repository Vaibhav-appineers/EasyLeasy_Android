package com.app.easyleasy.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.ResetPasswordRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class ResetPasswordViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val resetPasswordRepository: ResetPasswordRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val resetPasswordLiveData = MutableLiveData<TAListResponse<JsonElement>>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun callResetPassword(newPassword: String, mobileNumber: String, resetKey: String) {
        compositeDisposable.addAll(
            resetPasswordRepository.callResetPassword(newPassword, mobileNumber, resetKey)
                .subscribeOn(schedulerProvider.io())
                    .subscribe({ response ->
                        resetPasswordLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    })
        )
    }
}