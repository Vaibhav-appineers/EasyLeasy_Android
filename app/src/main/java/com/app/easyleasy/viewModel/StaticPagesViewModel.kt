package com.app.easyleasy.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.StaticPageResponse
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.StaticPagesRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class StaticPagesViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val staticPagesRepository: StaticPagesRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val updateTNCResponseLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val staticPageResponseLiveData = MutableLiveData<TAListResponse<StaticPageResponse>>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }


    fun callStaticPage(pageCode: String) {
        compositeDisposable.addAll(
            staticPagesRepository.getStaticPageData(pageCode)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        staticPageResponseLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    /**
     * Call update privacy policy api
     */
    fun callUpdateTNCPrivacyPolicy(pageType: String) {
        compositeDisposable.addAll(
            staticPagesRepository.updateTNCPrivacyPolicy(pageType)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        updateTNCResponseLiveData.postValue(response)
                    },
                    { error ->
                        messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }
}