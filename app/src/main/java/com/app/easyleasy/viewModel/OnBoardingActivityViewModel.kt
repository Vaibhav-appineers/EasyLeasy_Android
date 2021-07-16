package com.app.easyleasy.viewModel

import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.mvvm.BaseViewModel
import io.reactivex.disposables.CompositeDisposable

class OnBoardingActivityViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper){

    override fun onCreate() {

    }

}