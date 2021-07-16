package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.VersionConfigResponse
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.LoginResponse
import io.reactivex.Single
import javax.inject.Inject

class HomeRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun callConfigParameters(): Single<TAListResponse<VersionConfigResponse>> =
        networkService.callConfigParameters()

    fun callBuySubscription(map: HashMap<String, String>): Single<TAListResponse<LoginResponse>> =
        networkService.callBuySubscription(map = map)
}