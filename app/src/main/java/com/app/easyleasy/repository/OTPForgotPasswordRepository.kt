package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.forgotpasswordwithphone.ResetWithPhone
import io.reactivex.Single
import javax.inject.Inject

class OTPForgotPasswordRepository @Inject constructor(
    val networkService: NetworkService
){
    fun getOTPForgotPasswordPhoneResponse(mobileNumber: String): Single<TAListResponse<ResetWithPhone>> = networkService.callForgotPasswordWithPhone(mobileNumber)
}