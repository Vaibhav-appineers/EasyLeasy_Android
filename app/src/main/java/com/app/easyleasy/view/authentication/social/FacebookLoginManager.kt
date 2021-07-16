package com.app.easyleasy.view.authentication.social

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.dataclasses.Social
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.hb.logger.Logger


class FacebookLoginManager : AppCompatActivity() {
    private var callbackManager: CallbackManager? = null
    private var loginButton: LoginButton? = null
    private var social: Social? = null
    private val logger by lazy {
        Logger(this::class.java.simpleName)
    }
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoginManager.getInstance().logOut()
        //showProgress()
        social = Social("", "", "", "", "", "", "")

        if (loginButton == null) {
            loginButton = LoginButton(this)
            LoginManager.getInstance().logOut()

            callbackManager = CallbackManager.Factory.create()

            loginButton?.setPermissions(listOf("email"))

            loginButton?.performClick()


            // Callback registration
            loginButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                   // hideProgress()
                    setUserDetails(loginResult.accessToken)
                }

                override fun onCancel() {
                    logger.debugEvent("Facebook Login Failed", "Canceled by user")
                    onExit(false)
                }

                override fun onError(exception: FacebookException) {
                    logger.debugEvent("Facebook Login Failed", exception.toString())
                    onExit(false)
                }

            })
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }


    fun setUserDetails(accessToken: AccessToken) {

        val request = GraphRequest.newMeRequest(accessToken) { mJSONObject, _ ->

            try {
                val pictureObject = mJSONObject.getJSONObject("picture")
                val dataObject = pictureObject.getJSONObject("data")
                val picUrl = dataObject.optString("url")
                social?.profileImageUrl = picUrl
                social?.name = mJSONObject.optString("name").toString()
                social?.emailId = mJSONObject.optString("email").toString()
                social?.accessToken = accessToken.token
                social?.firstName = mJSONObject.optString("first_name").toString()
                social?.lastName = mJSONObject.optString("last_name").toString()
                social?.socialId = mJSONObject.optString("id").toString()
                social?.type = IConstants.SOCIAL_TYPE_FB
                onExit(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val parameters = Bundle()
        parameters.putString("fields",
            "id,name,link,email,first_name,last_name,picture.type(large)")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun onExit(isSuccess: Boolean) {
        //hideProgress()

        val intent = Intent()
        if (isSuccess) {
            intent.putExtra("facebook_data", social)
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

}