package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.LoginResponse
import io.reactivex.Single
import javax.inject.Inject

class LoginWithPhoneNumberSocialRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun callLoginWithPhoneNumberSocial(map: HashMap<String, String>): Single<TAListResponse<LoginResponse>> =
        networkService.loginWithSocial(map = map)

    fun callLoginWithPhoneNumber(map: HashMap<String, String>): Single<TAListResponse<LoginResponse>> =
        networkService.callLoginWithPhone(map = map)

}