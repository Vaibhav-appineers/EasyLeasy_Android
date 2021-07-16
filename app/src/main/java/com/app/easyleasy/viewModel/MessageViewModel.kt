package com.app.easyleasy.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.mvvm.BaseViewModel
import com.app.easyleasy.repository.MessageRepository
import io.reactivex.disposables.CompositeDisposable

class MessageViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val messageRepository: MessageRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

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
}