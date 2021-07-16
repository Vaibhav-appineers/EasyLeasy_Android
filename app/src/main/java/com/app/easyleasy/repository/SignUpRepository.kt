package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.app.easyleasy.dataclasses.response.OTPResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class SignUpRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun callSignUpWithEmail(map: HashMap<String, RequestBody>, file: MultipartBody.Part?): Single<TAListResponse<JsonElement>> =
        networkService.callSignUpWithEmail(fieldMap = map,file=file)

    fun callSignUpWithSocial(map: HashMap<String, RequestBody>, file: MultipartBody.Part?): Single<TAListResponse<LoginResponse>> =
        networkService.callSignUpWithSocial(fieldMap = map,file=file)

    fun callCheckUniqueUser(map: HashMap<String, String>): Single<TAListResponse<OTPResponse>> =
        networkService.callCheckUniqueUser(map = map)


}
