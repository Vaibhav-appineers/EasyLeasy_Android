package com.app.easyleasy.api.network

interface InternetConnectivityListener {

    fun onInternetConnectivityChanged(isConnected: Boolean)
}