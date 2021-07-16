package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class LoginWithEmailRepository @Inject constructor(val networkService: NetworkService) {

    fun callLoginWithEmail(map: HashMap<String, String>): Single<TAListResponse<LoginResponse>> =
        networkService.loginWithEmail(map = map)

    fun callResendLink(map: HashMap<String, String>): Single<TAListResponse<JsonElement>> =
        networkService.callSendVerificationLink(map = map)
}
