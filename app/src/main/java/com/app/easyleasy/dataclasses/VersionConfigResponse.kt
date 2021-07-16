package com.app.easyleasy.dataclasses

import android.content.Context
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.extension.getAppVersion
import com.app.easyleasy.dataclasses.response.PurchasedSubscription
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class VersionConfigResponse(
    @JsonProperty("version_update_optional")
    val versionUpdateOptional: String? = null,

    @JsonProperty("iphone_version_number")
    val iphoneVersionNumber: String? = null,

    @JsonProperty("android_version_number")
    val androidVersionNumber: String? = null,

    @JsonProperty("version_update_check")
    val versionUpdateCheck: String? = null,

    @JsonProperty("version_check_message")
    val versionCheckMessage: String? = null,

    @JsonProperty("terms_conditions_updated")
    val termsConditionsUpdated: String? = null,

    @JsonProperty("privacy_policy_updated")
    val privacyPolicyUpdated: String? = null,

    @JsonProperty("log_status_updated")
    val logStatusUpdated: String = "",

    @JsonProperty("android_app_id")
    var androidAppId: String? = null,

    @JsonProperty("android_banner_id")
    var androidBannerId: String? = null,

    @JsonProperty("android_interstitial_id")
    var androidInterstitialId: String? = null,

    @JsonProperty("android_native_id")
    var androidNativeId: String? = null,

    @JsonProperty("android_rewarded_id")
    var androidRewardedId: String? = null,

    @JsonProperty("project_debug_level")
    var projectDebugLevel: String? = null,

    @JsonProperty("android_mopub_banner_id")
    val androidMoPubBannerId: String? = null,

    @JsonProperty("android_mopub_interstitial_id")
    val androidMopubInterstitialId: String? = null,

    @JsonProperty("subscription")
    val subscription: ArrayList<PurchasedSubscription>? = null

) {
    fun shouldShowVersionDialog(context: Context) =
        (((androidVersionNumber?.compareTo(getAppVersion(context, true))
            ?: 0) > 0) && versionUpdateCheck.equals("1"))

    fun isOptionalUpdate() = versionUpdateOptional.equals("1")

    fun shouldShowTNCUpdated() = termsConditionsUpdated.equals("1")

    fun shouldShowPrivacyPolicyUpdated() = privacyPolicyUpdated.equals("1")

    fun isAppInDevelopment() = projectDebugLevel.equals("development", true)

    fun isSubscribed(): Boolean {
        var isSubscribed = false
        if (subscription != null && subscription.size > 0 && subscription.filter { it.subscriptionStatus == "1" && (it.productId == IConstants.ANDROID_MONTHLY_SUB_ID || it.productId == IConstants.IOS_MONTHLY_SUB_ID) }.isNotEmpty()) {
            isSubscribed = true
        }
        return isSubscribed
    }

    fun isAdsFree(): Boolean {
        var isAddsFree = false
        if (subscription != null && subscription.size > 0 && subscription.filter { it.subscriptionStatus == "1" && (it.productId == IConstants.ANDROID_AD_FREE_ID || it.productId == IConstants.IOS_AD_FREE_ID) }.isNotEmpty()) {
            isAddsFree = true
        }
        return isAddsFree
    }

}