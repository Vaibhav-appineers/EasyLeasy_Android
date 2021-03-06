@file:Suppress("DEPRECATION")

package com.app.easyleasy.db

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.app.easyleasy.R
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.google.gson.GsonBuilder

/**
 * This class is used for storing and retrieving shared preference values.
 */

@Suppress("DEPRECATION")
class AppPrefrrences(context: Context) {

    private val gson = GsonBuilder().create()

    private fun <T> getObjectFromJsonString(jsonData: String, modelClass: Class<*>): Any? {
        return gson.fromJson(jsonData, modelClass)
    }

    private fun getJsonStringFromObject(modelClass: Any): String {
        return gson.toJson(modelClass)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    @RequiresApi(Build.VERSION_CODES.M)
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    val pref = EncryptedSharedPreferences
            .create(
                    context.getString(R.string.app_name),
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

    /**
     * Store and get wether user is loggedin or not
     */
    var isLogin: Boolean
        get() = pref.getBoolean("isLogin", false)
        set(value) = pref.edit().putBoolean("isLogin", value).apply()

    /**
     * Store and get firebase token
     */
    var deviceToken: String?
        get() = pref.getString("deviceToken", "")
        set(value) = pref.edit().putString("deviceToken", value).apply()

    /**
     * Store and get logged-in user auth token, which is used for calling apis
     */
    var authToken: String?
        get() = pref.getString("authToken", "")
        set(value) = pref.edit().putString("authToken", value).apply()

    var isSkip: Boolean
        get() = pref.getBoolean("isSkip", false)
        set(value) = pref.edit().putBoolean("isSkip", value).apply()

    /**
     * Boolean value for is ad removed or not
     */
    var isAdRemoved: Boolean
        get() = pref.getBoolean("isAdRemoved", false)
        set(value) = pref.edit().putBoolean("isAdRemoved", value).apply()

    /**
     * Store and get logged in user details
     */
    var userDetail: LoginResponse?
        get() = getObjectFromJsonString<LoginResponse>(pref.getString("userDetail", "")
                ?: "", LoginResponse::class.java) as LoginResponse?
        set(value) =
            if (value == null) {
                pref.edit().putString("userDetail", "").apply()
            } else {
                pref.edit().putString("userDetail", getJsonStringFromObject(value)).apply()
            }

    var notificationDefaultChannelSet: Boolean
        get() = pref.getBoolean(IConstants.SP_NOTIFICATION_CHANNEL_DEFAULT, false)
        set(value) = pref.edit().putBoolean(IConstants.SP_NOTIFICATION_CHANNEL_DEFAULT, value).apply()

    /**
     * Store application login type i.e. login with email, phone, email+social and phone+social
     */
    var appLoginType: String?
        get() = pref.getString("loginType", "")
        set(value) = pref.edit().putString("loginType", value).apply()

    /**
     * Boolean value for app rating status
     */
    var isAppRatingDone: Boolean
        get() = pref.getBoolean("appRatingStatus", false)
        set(value) = pref.edit().putBoolean("appRatingStatus", value).apply()

    /**
     * timestamp value of first app launch
     */
    var ratingInitTime: Long
        get() = pref.getLong("ratingInitTime", 0)
        set(value) = pref.edit().putLong("ratingInitTime", value).apply()

    /**
     * app launch count
     */
    var appLaunchCount: Int
        get() = pref.getInt("launchCount", 0)
        set(value) = pref.edit().putInt("launchCount", value).apply()

    /**
     * log status flag
     * */
    var logStatusUpdated:String?
        get() = pref.getString("logStatusUpdated", "")
        set(value) = pref.edit().putString("logStatusUpdated", value).apply()

    /**
     * Boolean value for on-boarding screen
     */
    var isOnBoardingShown: Boolean
        get() = pref.getBoolean("isOnBoardingShown", false)
        set(value) = pref.edit().putBoolean("isOnBoardingShown", value).apply()

    /**
     * project debug level vale from get_config_api, used for admob ads
     * */
    var projectDebugLevel: String?
        get() = pref.getString("projectDebugLevel", "")
        set(value) = pref.edit().putString("projectDebugLevel", value).apply()

    /**
     *  Admob ad Banner unit id
     * */
    var androidBannerId: String?
        get() = pref.getString("androidBannerId", "")
        set(value) = pref.edit().putString("androidBannerId", value).apply()

    /**
     *  Admob ad Interstitial unit id
     * */
    var androidInterstitialId: String?
        get() = pref.getString("androidInterstitialId", "")
        set(value) = pref.edit().putString("androidInterstitialId", value).apply()

    /**
     *  Admob ad Reward unit id
     * */
    var androidRewardedId: String?
        get() = pref.getString("androidRewardedId", "")
        set(value) = pref.edit().putString("androidRewardedId", value).apply()

    /**
     *  Admob ad Native unit id
     * */
    var androidNativeId: String?
        get() = pref.getString("androidNativeId", "")
        set(value) = pref.edit().putString("androidNativeId", value).apply()

    /**
     *  MoPub Banner Ad unit id
     * */
    var androidMoPubBannerId: String?
        get() = pref.getString("androidMoPubBannerId", "")
        set(value) = pref.edit().putString("androidMoPubBannerId", value).apply()

    /**
     *  MoPub Interstitial Ad unit id
     * */
    var androidMopubInterstitialId: String?
        get() = pref.getString("androidMopubInterstitialId", "")
        set(value) = pref.edit().putString("androidMopubInterstitialId", value).apply()
}