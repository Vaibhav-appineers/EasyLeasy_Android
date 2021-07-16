package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.StaticPageResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class StaticPagesRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun getStaticPageData(code: String): Single<TAListResponse<StaticPageResponse>> =
        networkService.callGetStaticPageData(code)

    fun updateTNCPrivacyPolicy(type: String): Single<TAListResponse<JsonElement>> =
        networkService.callUpdateTNCPrivacyPolicy(type)
}