package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class ResetPasswordRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun callResetPassword(newPassword: String, mobileNumber: String, resetKey: String)
            : Single<TAListResponse<JsonElement>> =
        networkService.callResetPassword(newPassword, mobileNumber, resetKey)
}