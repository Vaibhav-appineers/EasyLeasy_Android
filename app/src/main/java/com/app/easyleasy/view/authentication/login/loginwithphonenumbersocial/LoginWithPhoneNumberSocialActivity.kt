package com.app.easyleasy.view.authentication.login.loginwithphonenumbersocial

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.commonUtils.common.isValidMobileNumber
import com.app.easyleasy.commonUtils.common.isValidMobileNumberLength
import com.app.easyleasy.commonUtils.common.isValidText
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.extension.isValidPassword
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityLoginWithPhoneNumberSocialBinding
import com.app.easyleasy.dataclasses.Social
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.authentication.forgotpassword.phone.ForgotPasswordWithPhoneActivity
import com.app.easyleasy.view.authentication.signup.SignUpActivity
import com.app.easyleasy.view.authentication.social.AppleLoginManager
import com.app.easyleasy.view.authentication.social.FacebookLoginManager
import com.app.easyleasy.view.authentication.social.GoogleLoginManager
import com.app.easyleasy.viewModel.LoginWithPhoneNumberSocialViewModel
import com.google.android.material.textfield.TextInputEditText
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import timber.log.Timber

class LoginWithPhoneNumberSocialActivity : BaseActivity<LoginWithPhoneNumberSocialViewModel>() {

    private var binding: ActivityLoginWithPhoneNumberSocialBinding? = null
    private lateinit var social: Social

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_with_phone_number_social)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        (application as AppineersApplication).setApplicationLoginType(IConstants.LOGIN_TYPE_PHONE_SOCIAL)
        setFireBaseAnalyticsData("id-loginWithPhoneNumberSocialScreen","view_loginWithPhoneNumberSocialScreen","view_loginWithPhoneNumberSocialScreen")
        initListeners()
    }

    private fun initListeners() {
        binding?.let {
            with(it, {

                tietPhoneNumber.addTextChangedListener(
                    PhoneNumberFormattingTextWatcher("US")
                )

                btnLogin.setOnClickListener {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "user login"
                    )
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Login Button Click")
                    hideKeyboard()
                    binding?.tietPassword?.let { it1 ->
                        binding?.tietPhoneNumber?.let { it2 ->
                            validateAndSendRequest(
                                it2,
                                it1
                            )
                        }
                    }
                }

                ibtnFacebook.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Facebook Login Button Click")
                    when {
                        checkInternet() -> {
                            MSCGenerator.addAction(
                                GenConstants.ENTITY_USER,
                                GenConstants.ENTITY_APP,
                                "facebook login"
                            )
                            facebookLogin()
                        }
                    }

                }

                ibtnGoogle.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Google Login Button Click")
                    when {
                        checkInternet() -> {
                            MSCGenerator.addAction(
                                GenConstants.ENTITY_USER,
                                GenConstants.ENTITY_APP,
                                "google login"
                            )
                            googleLogin()
                        }
                    }
                }

                ibtnApple.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Apple Login Button Click")
                    when {
                        checkInternet() -> {
                            appleLogin()
                        }
                    }
                }

                tvCreateNewAccount.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Sign up Button Click")
                    hideKeyboard()
                    startActivity(
                        Intent(
                            this@LoginWithPhoneNumberSocialActivity,
                            SignUpActivity::class.java
                        )
                    )
                }

                tvForgotPassword.setOnClickListener {
                    hideKeyboard()
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Forgot Password Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "forgot password"
                    )
                    startActivity(
                        Intent(
                            this@LoginWithPhoneNumberSocialActivity,
                            ForgotPasswordWithPhoneActivity::class.java
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
                            binding?.tietPhoneNumber?.let { it2 ->
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
        val intent = Intent(this@LoginWithPhoneNumberSocialActivity, GoogleLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_GOOGLE_LOGIN)
    }

    /**
     * Perform Apple Login
     */
    private fun appleLogin() {
        hideKeyboard()
        val intent = Intent(this@LoginWithPhoneNumberSocialActivity, AppleLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_APPLE_LOGIN)
    }

    /**
     * Perform Facebook Login
     */
    private fun facebookLogin() {
        hideKeyboard()
        val intent = Intent(this@LoginWithPhoneNumberSocialActivity, FacebookLoginManager::class.java)
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
                        viewModel.callLoginWithSocial(
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
                        viewModel.callLoginWithSocial(
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
                        viewModel.callLoginWithSocial(
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


    //Method to validate data and call api request method
    private fun validateAndSendRequest(
        tietUsername: TextInputEditText,
        tietPassword: TextInputEditText
    ) {
        val phoneNumber = PhoneNumberUtils.normalizeNumber(tietUsername.text.toString())
        val password = tietPassword.text.toString()
        val validatePhoneNumber =
            phoneNumber.isValidMobileNumber() && phoneNumber.isValidMobileNumberLength()
        val validatePassword = password.isValidPassword()
        val validateTextPassword = password.isValidText()
        when {
            !validatePhoneNumber -> {
                tietUsername.error = getString(R.string.alert_invalid_phone_number)
                  showMessage(
                    getString(R.string.alert_invalid_phone_number)
                )
            }
            !validateTextPassword -> {
                tietPassword.error = getString(R.string.alert_enter_password)
                showMessage(getString(R.string.alert_enter_password))
            }
            !validatePassword -> {
                tietPassword.error = getString(R.string.alert_valid_password)
                  showMessage(
                    getString(R.string.alert_valid_password)
                )
            }
            else -> {
                tietUsername.error = null
                tietPassword.error = null
                callLoginWithPhoneNumber(phoneNumber = phoneNumber, password = password)
            }
        }
    }

    //Method to get forgot password response with emailId
    private fun callLoginWithPhoneNumber(phoneNumber: String, password: String) {
        hideKeyboard()
        showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
        viewModel.callLoginWithPhoneNumber(phoneNumber = phoneNumber, password = password)
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.loginSocialMutableLiveData.observe(this, Observer{ response ->
            hideProgressDialog()
            when (response.settings?.success) {
                "1" -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "logged in success"
                    )
                      showMessage( response.settings!!.message)
                    viewModel.saveUserDetails(response?.data!![0])
                    navigateToHomeScreen()
                }
                "2" -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "sign up screen"
                    )
                    startActivity(
                        SignUpActivity.getStartIntent(
                            mContext = this@LoginWithPhoneNumberSocialActivity,
                            social = social
                        )
                    )
                }
                else -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "logged in failed"
                    )
                      showMessage( response.settings!!.message)
                    Timber.d(response.settings?.message)
                }
            }
        })

        viewModel.phoneNumberLoginMutableLiveData.observe(this, Observer{ response ->
            hideProgressDialog()
            when (response.settings?.isSuccess) {
                true -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "logged in success"
                    )
                    showMessage( response.settings!!.message)
                    viewModel.saveUserDetails(response.data!![0])
                    navigateToHomeScreen()
                }
                else -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "logged in failed"
                    )
                    showMessage( response.settings!!.message)
                    Timber.d(response.settings?.message)
                }
            }
        })
    }
}