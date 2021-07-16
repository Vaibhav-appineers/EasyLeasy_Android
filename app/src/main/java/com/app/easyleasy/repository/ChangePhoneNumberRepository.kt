package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import javax.inject.Inject

class ChangePhoneNumberRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun checkUniqueUser(map: HashMap<String, String>) = networkService.callCheckUniqueUser(map)
}