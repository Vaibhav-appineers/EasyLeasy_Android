package com.app.easyleasy.objectclasses

import com.app.easyleasy.utils.TestSchedulerProvider
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.utils.mock
import io.reactivex.disposables.CompositeDisposable

open class KotlinBaseMockObjectsClass {
    val testSchedulerProvider = TestSchedulerProvider()
    val mockCompositeDisposable = mock<CompositeDisposable>()
    val mockNetworkService = mock<NetworkService>()
    val mockApplication = mock<AppineersApplication>()
    val mockNetworkHelper = mock<NetworkHelper>()
    val mockDataRepository = mock<WeatherDataRepository>()
}