package com.app.easyleasy.api.network


import com.app.easyleasy.BuildConfig
import com.app.easyleasy.api.network.tokens.TokenAuth
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.hb.logger.msc.MSCGenInterceptor
import com.hb.logger.util.LoggerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object Networking {

    private const val NETWORK_CALL_TIMEOUT = 60
    private lateinit var API_KEY: String

    fun create(
        apiKey: String,
        baseUrl: String,
        application: AppineersApplication
    ): NetworkService {
        var interceptorLevel = HttpLoggingInterceptor.Level.NONE
        interceptorLevel = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else if (!BuildConfig.DEBUG && sharedPreference.logStatusUpdated == "active") {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        val logger = HttpLoggingInterceptor()
        logger.level = interceptorLevel

        /**
         * Code to create Retrofit api call
         */
        API_KEY = apiKey
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(TokenAuth(application))
                    .addInterceptor(
                        HttpLoggingInterceptor()
                            .apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            })
                    .addInterceptor(RequestInterceptor(object :
                        RequestInterceptor.OnRequestInterceptor { //to provide common header & params
                        override fun provideHeaderMap(): HashMap<String, String> {
                            val map = HashMap<String, String>()
                            if (sharedPreference.authToken?.isNotEmpty() == true)
                                map["AUTHTOKEN"] = sharedPreference.authToken.toString()
                            return map
                        }

                        override fun provideBodyMap(): HashMap<String, String> {
                            return HashMap()
                        }
                    }))
                    .addInterceptor(LoggerInterceptor())
                    .addInterceptor(MSCGenInterceptor())
                    .addInterceptor(logger)
                    .readTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .writeTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .connectTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(JacksonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(NetworkService::class.java)
    }
}