package com.app.easyleasy.dataclasses.response

import android.content.Context
import com.app.easyleasy.commonUtils.utility.extension.getAppVersion
import com.google.gson.annotations.SerializedName
data class VersionConfigResponse(

        @field:SerializedName("version_update_optional")
        val versionUpdateOptional: String? = null,

        @field:SerializedName("iphone_version_number")
        val iphoneVersionNumber: String? = null,

        @field:SerializedName("android_version_number")
        val androidVersionNumber: String? = null,

        @field:SerializedName("version_update_check")
        val versionUpdateCheck: String? = null,

        @field:SerializedName("version_check_message")
        val versionCheckMessage: String? = null,

        @field:SerializedName("terms_conditions_updated")
        val termsConditionsUpdated: String? = null,

        @field:SerializedName("privacy_policy_updated")
        val privacyPolicyUpdated: String? = null,

        @field:SerializedName("log_status_updated")
        val logStatusUpdated: String? = null

) {
    fun shouldShowVersionDialog(context: Context) =
            (((androidVersionNumber?.compareTo(getAppVersion(context, true))
                    ?: 0) > 0) && versionUpdateCheck.equals("1"))

    fun isOptionalUpdate() = versionUpdateOptional.equals("1")

    fun shouldShowTNCUpdated() = termsConditionsUpdated.equals("1")

    fun shouldShowPrivacyPolicyUpdated() = privacyPolicyUpdated.equals("1")

}