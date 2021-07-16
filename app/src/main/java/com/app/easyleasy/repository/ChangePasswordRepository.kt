package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class ChangePasswordRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun callChangePassword(oldPassword: String, newPassword: String)
    : Single<TAListResponse<JsonElement>> = networkService.callChangePassword(oldPassword,newPassword)
}