package com.app.easyleasy.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.forgotpasswordwithphone.ResetWithPhone
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.ForgotPasswordPhoneRepository
import io.reactivex.disposables.CompositeDisposable

class ForgotPasswordPhoneViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        private val forgotPasswordPhoneRepository: ForgotPasswordPhoneRepository
)
    : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val forgotPasswordWithPhoneLiveData = MutableLiveData<TAListResponse<ResetWithPhone>>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    /**
     * Api call for getting password via phone
     */
    fun getForgotPasswordWithPhone(mobileNumber: String) {
        compositeDisposable.addAll(
                forgotPasswordPhoneRepository.getForgotPasswordPhoneResponse(mobileNumber)
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                                { response ->
                                    forgotPasswordWithPhoneLiveData.postValue(response)
                                },
                                { error ->
                                    messageString.postValue(Resource.error(error.message))
                                }
                        )
        )
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }
}