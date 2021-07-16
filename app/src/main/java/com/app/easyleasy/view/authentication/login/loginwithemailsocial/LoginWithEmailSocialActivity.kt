package com.app.easyleasy.view.authentication.login.loginwithemailsocial

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.commonUtils.common.isValidEmail
import com.app.easyleasy.commonUtils.common.isValidText
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.dialog.DialogUtil
import com.app.easyleasy.commonUtils.utility.extension.getTrimText
import com.app.easyleasy.commonUtils.utility.extension.isValidPassword
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityLoginWithEmailSocialBinding
import com.app.easyleasy.dataclasses.Social
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.authentication.forgotpassword.email.ForgotPasswordWithEmailActivity
import com.app.easyleasy.view.authentication.signup.SignUpActivity
import com.app.easyleasy.view.authentication.social.AppleLoginManager
import com.app.easyleasy.view.authentication.social.FacebookLoginManager
import com.app.easyleasy.view.authentication.social.GoogleLoginManager
import com.app.easyleasy.viewModel.LoginWithEmailSocialViewModel
import com.google.android.material.textfield.TextInputEditText
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class LoginWithEmailSocialActivity : BaseActivity<LoginWithEmailSocialViewModel>() {

    private var binding: ActivityLoginWithEmailSocialBinding? = null
    private lateinit var social: Social

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_with_email_social)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        (application as AppineersApplication).setApplicationLoginType(IConstants.LOGIN_TYPE_EMAIL_SOCIAL)
        initListeners()
        printHashKey()
    }


    private fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(
                "com.app.easyleasy",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private fun initListeners() {
        binding?.let {
            with(it, {
                mbtnLogin.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Login Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "user login"
                    )
                    hideKeyboard()
                    binding?.tietPassword?.let { it1 ->
                        binding?.tietUsername?.let { it2 ->
                            validateAndSendRequest(
                                it2,
                                it1
                            )
                        }
                    }
                }

                ibtnFacebook.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Facebook Login Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "facebook login"
                    )
                    when {
                        checkInternet() -> {
                            facebookLogin()
                        }
                    }

                }

                ibtnGoogle.setOnClickListener {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "google login"
                    )
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Google Login Button Click")
                    when {
                        checkInternet() -> {
                            googleLogin()
                        }
                    }
                }

                ibtnApple.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Apple Login Button Click")
                    MSCGenerator.addAction(GenConstants.ENTITY_USER, GenConstants.ENTITY_APP, "Apple login")
                    when {
                        checkInternet() -> {
                            appleLogin()
                        }
                    }

                }

                mbtnSkip.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Skip Button Click")
                    AppineersApplication.sharedPreference.isSkip = true
                    navigateToHomeScreen()
                }

                tvCreateNewAccount.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Sign up Button Click")
                    MSCGenerator.addAction(GenConstants.ENTITY_USER, GenConstants.ENTITY_APP, "Sign up Button Click")
                    hideKeyboard()
                    startActivity(
                        Intent(
                            this@LoginWithEmailSocialActivity,
                            SignUpActivity::class.java
                        )
                    )
                }

                tvForgotPassword.setOnClickListener {
                    hideKeyboard()
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "forgot password"
                    )
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Forgot Password Button Click")
                    startActivity(
                        Intent(
                            this@LoginWithEmailSocialActivity,
                            ForgotPasswordWithEmailActivity::class.java
                        )
                    )
                }

                tietPassword.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                    hideKeyboard()
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Login Button Click")
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "user login"
                        )
                        binding?.tietPassword?.let { it1 ->
                            binding?.tietUsername?.let { it2 ->
                                validateAndSendRequest(
                                    it2,
                                    it1
                                )
                            }
                        }
                        return@OnEditorActionListener true
                    }
                    false
                })
            })
        }
    }

    /**
     * Perform Google Login
     */
    private fun googleLogin() {
        hideKeyboard()
        val intent = Intent(this@LoginWithEmailSocialActivity, GoogleLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_GOOGLE_LOGIN)
    }

    /**
     * Perform Apple Login
     */
    private fun appleLogin() {
        hideKeyboard()
        val intent = Intent(this@LoginWithEmailSocialActivity, AppleLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_APPLE_LOGIN)
    }

    /**
     * Perform Facebook Login
     */
    private fun facebookLogin() {
        hideKeyboard()
        val intent = Intent(this@LoginWithEmailSocialActivity, FacebookLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_FACEBOOK_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IConstants.REQUEST_CODE_FACEBOOK_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("facebook_data")
                    if (mSocial != null) {
                        social = mSocial

                        showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                        viewModel.callLoginWithEmailSocial(
                            socialType = IConstants.SOCIAL_TYPE_FB,
                            socialId = social.socialId ?: ""
                        )
                    } else {
                        showMessage(
                            getString(R.string.msg_no_user_data)
                        )
                    }
                }

                IConstants.REQUEST_CODE_GOOGLE_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("google_data")
                    if (mSocial != null) {
                        social = mSocial
                        showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                        viewModel.callLoginWithEmailSocial(
                            socialType = IConstants.SOCIAL_TYPE_GOOGLE,
                            socialId = social.socialId ?: ""
                        )
                    } else {
                        showMessage(
                            getString(R.string.msg_no_user_data)
                        )
                    }
                }

                IConstants.REQUEST_CODE_APPLE_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("apple_data")
                    if (mSocial != null) {
                        social = mSocial
                        showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                        viewModel.callLoginWithEmailSocial(
                            socialType = IConstants.SOCIAL_TYPE_APPLE,
                            socialId = social.socialId ?: ""
                        )
                    } else {
                        showMessage(
                            getString(R.string.msg_no_user_data)
                        )
                    }
                }
            }
        }
    }

    /**
     * Show email not verified dialog
     */
    private fun showResendEmailDialog(message: String) {
        DialogUtil.alert(
            context = this@LoginWithEmailSocialActivity,
            msg = message + "\n" + getString(R.string.msg_resend_email),
            positiveBtnText = getString(R.string.resend),
            negativeBtnText = getString(R.string.cancel),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    //showProgress()
                    binding?.tietUsername?.getTrimText()
                        ?.let {
                            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                            viewModel.callResendLink(email = it)
                        }
                }

                override fun onCancel(isNeutral: Boolean) {

                }
            },
            isCancelable = false
        )
    }


    //Method to validate data and call api request method
    private fun validateAndSendRequest(
        tietUsername: TextInputEditText,
        tietPassword: TextInputEditText
    ) {
        val email = tietUsername.text.toString()
        val password = tietPassword.text.toString()
        val validTextEmail = email.isValidText()
        val validateEmail = email.isValidEmail()
        val validateTextPassword = password.isValidText()
        val validatePassword = password.isValidPassword()
        when {
            !validTextEmail -> {
                showMessage(
                    getString(R.string.alert_enter_email)
                )
            }

            !validateEmail -> {
                showMessage(
                    getString(R.string.alert_enter_valid_email)
                )
            }

            !validateTextPassword -> {
                showMessage(getString(R.string.alert_enter_password))
            }
            !validatePassword -> {
                showMessage(getString(R.string.alert_valid_password))
            }
            else -> {
                tietUsername.error = null
                tietPassword.error = null
                //viewModel.showDialog.postValue(true)
                callLoginWithEmail(email = email, password = password)
            }
        }
    }

    //Method to get forgot password response with emailId
    private fun callLoginWithEmail(email: String, password: String) {
        hideKeyboard()
        showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.callLoginWithEmail(email = email, password = password)
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.loginEmailMutableLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            when {
                response.settings?.isSuccess == true -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "logged in success"
                    )
                    viewModel.saveUserDetails(response.data!![0])
                    navigateToHomeScreen()
                }
                response.settings?.success.equals("3") -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "resend email"
                    )
                    showResendEmailDialog(response.settings?.message ?: "")
                }
                else -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "logged in failed"
                    )
                    showMessage(response.settings!!.message)
                    Timber.d(response.settings?.message)
                }
            }
        })

        viewModel.loginSocialMutableLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            when (response.settings?.success) {
                "1" -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "social login success"
                    )
                    showMessage(response.settings!!.message)
                    viewModel.saveUserDetails(response?.data!![0])
                    navigateToHomeScreen()
                }
                "2" -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "social login success"
                    )
                    startActivity(
                        SignUpActivity.getStartIntent(
                            mContext = this@LoginWithEmailSocialActivity,
                            social = social
                        )
                    )
                }
                else -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "social login failed"
                    )
                    showMessage(response.settings!!.message)
                    Timber.d(response.settings?.message)
                }
            }
        })

        viewModel.resendLinkMutableLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "resend email success"
                )
                showMessage(response.settings!!.message)
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "resend email failed"
                )
                showMessage(response.settings!!.message)
                Timber.d(response.settings?.message)
            }
        })
    }
}


