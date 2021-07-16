package com.app.easyleasy.repository

import com.app.easyleasy.api.network.NetworkService
import javax.inject.Inject

class FriendRepository @Inject constructor(
    val networkService: NetworkService
) {}