package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.OTPResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class ChangePhoneNumberOTPRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun checkUniqueUser(map: HashMap<String, String>): Single<TAListResponse<OTPResponse>> =
        networkService.callCheckUniqueUser(map)

    fun callChangePhoneNumber(number: String): Single<TAListResponse<JsonElement>> =
        networkService.callChangePhoneNumber(number)
}