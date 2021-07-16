package com.app.easyleasy.dagger.components

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.app.easyleasy.api.network.NetworkHelper
import com.app.easyleasy.api.network.NetworkService
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.commonUtils.rx.SchedulerProvider
import com.app.easyleasy.dagger.ApplicationContext
import com.app.easyleasy.dagger.modules.ApplicationModule
import dagger.Component
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Singleton


@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    fun inject(app: AppineersApplication)

    fun getApplication(): Application

    @ApplicationContext
    fun getContext(): Context

    fun getNetworkService(): NetworkService

    fun getNetworkHelper(): NetworkHelper

    fun getSchedulerProvider(): SchedulerProvider

    fun getCompositeDisposable(): CompositeDisposable

    fun getAlertDialogBuilder(): AlertDialog.Builder

    fun getLayoutInflater(): LayoutInflater
}