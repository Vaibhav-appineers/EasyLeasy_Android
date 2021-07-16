package com.app.easyleasy.view.authentication.login.loginwithphonenumber

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
import com.app.easyleasy.commonUtils.utility.extension.sharedPreference
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityLoginWithPhoneNumberBinding
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.authentication.forgotpassword.phone.ForgotPasswordWithPhoneActivity
import com.app.easyleasy.view.authentication.signup.SignUpActivity
import com.app.easyleasy.viewModel.LoginWithPhoneNumberViewModel
import com.google.android.material.textfield.TextInputEditText
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginWithPhoneNumberActivity : BaseActivity<LoginWithPhoneNumberViewModel>() {

    private var binding: ActivityLoginWithPhoneNumberBinding? = null

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_with_phone_number)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        (application as AppineersApplication).setApplicationLoginType(IConstants.LOGIN_TYPE_PHONE)
        setFireBaseAnalyticsData(
            "id-loginWithPhoneNumberScreen",
            "view_loginWithPhoneNumberScreen",
            "view_loginWithPhoneNumberScreen"
        )
        binding?.tietPhoneNumber?.addTextChangedListener(PhoneNumberFormattingTextWatcher("US"))
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

                mbtnSkip.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Skip Button Click")
                    sharedPreference.isSkip = true
                    navigateToHomeScreen()
                }

                tvCreateNewAccount.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Sign up Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Sign up Button Click"
                    )
                    hideKeyboard()
                    startActivity(
                        Intent(
                            this@LoginWithPhoneNumberActivity,
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
                            this@LoginWithPhoneNumberActivity,
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


    //Method to validate data and call api request method
    private fun validateAndSendRequest(
        tietPhoneNo: TextInputEditText,
        tietPassword: TextInputEditText
    ) {
        val phoneNumber = PhoneNumberUtils.normalizeNumber(tietPhoneNo.text.toString())
        val password = tietPassword.text.toString()
        val validatePhoneNumber =
            phoneNumber.isValidMobileNumber() && phoneNumber.isValidMobileNumberLength()
        val validatePassword = password.isValidPassword()
        val validateTextPassword = password.isValidText()
        when {
            !validatePhoneNumber -> {
                tietPhoneNo.error = getString(R.string.alert_enter_mobile_number)
                showMessage(
                    getString(R.string.alert_enter_mobile_number)
                )
            }
            !validateTextPassword -> {
                tietPassword.error = getString(R.string.alert_enter_password)
                showMessage(getString(R.string.alert_enter_password))
            }
            !validatePassword -> {
                tietPassword.error = getString(R.string.alert_valid_password)
                showMessage(getString(R.string.alert_valid_password))
            }
            else -> {
                tietPhoneNo.error = null
                tietPassword.error = null
                callLoginWithPhoneNumber(phoneNumber = phoneNumber, password = password)
            }
        }
    }

    //Method to get forgot password response with emailId
    private fun callLoginWithPhoneNumber(phoneNumber: String, password: String) {
        hideKeyboard()
        showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.callLoginWithPhoneNumber(phoneNumber = phoneNumber, password = password)
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.phoneNumberLoginMutableLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "logged in success"
                )
                showMessage(response.settings!!.message)
                viewModel.saveUserDetails(response.data!![0])
                navigateToHomeScreen()
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "logged in failed"
                )
                showMessage(response.settings!!.message)
                Timber.d(response.settings?.message)
            }
        })
    }
}