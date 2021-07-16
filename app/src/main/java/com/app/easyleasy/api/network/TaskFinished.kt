package com.app.easyleasy.api.network

interface TaskFinished<T> {
    fun onTaskFinished(data: T)
}