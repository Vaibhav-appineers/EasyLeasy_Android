package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun callLogout(): Single<TAListResponse<JsonElement>> = networkService.callLogOut()

    fun callDeleteAccount(): Single<TAListResponse<JsonElement>> =
        networkService.callDeleteAccount()

    fun callGoAdFree(map: HashMap<String, String>): Single<TAListResponse<JsonElement>> =
        networkService.callGoAdFree(map = map)

    fun callBuySubscription(map: HashMap<String, String>): Single<TAListResponse<LoginResponse>> =
        networkService.callBuySubscription(map = map)

}