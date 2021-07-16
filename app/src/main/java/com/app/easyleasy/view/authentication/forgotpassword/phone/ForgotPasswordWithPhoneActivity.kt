package com.app.easyleasy.view.authentication.forgotpassword.phone

import android.annotation.SuppressLint
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.R
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.commonUtils.common.isValidMobileNumber
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityForgotPasswordWithPhoneBinding
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.authentication.otp.otpforgotpassword.OTPForgotPasswordActivity
import com.app.easyleasy.viewModel.ForgotPasswordPhoneViewModel
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber

class ForgotPasswordWithPhoneActivity : BaseActivity<ForgotPasswordPhoneViewModel>() {
    private var dataBinding: ActivityForgotPasswordWithPhoneBinding? = null
    private var mobileNumber: String? = ""

    override fun setDataBindingLayout() {
        dataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_forgot_password_with_phone)
        dataBinding?.lifecycleOwner = this
        dataBinding?.viewModel = viewModel
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-forgotPasswordWithPhoneScreen","view_forgotPasswordWithPhoneScreen","view_forgotPasswordWithPhoneScreen")
        dataBinding?.let {
            with(it, {
                tietMobileNumber.doAfterTextChanged { text ->
                    mobileNumber =  PhoneNumberUtils.normalizeNumber(text?.toString()!!.trim())
                }

                tietMobileNumber.addTextChangedListener(
                    PhoneNumberFormattingTextWatcher("US")
                )

                ibtnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK,"Back Button Click")
                    finish()
                }

                mbtnNext.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK,"Next Button Click")
                    validateAndSendRequest(tietMobileNumber)
                }

                tietMobileNumber.setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> {
                            validateAndSendRequest(tietMobileNumber)
                            true
                        }
                        else -> false
                    }
                }
            })
        }
    }

    //Method to validate data and call api request method
    private fun validateAndSendRequest(tietMobileNumber: TextInputEditText) {
        val validation = mobileNumber!!.isValidMobileNumber()
        when {
            validation -> {
                tietMobileNumber.error = null
                getForgotPasswordDetailsWithPhone(PhoneNumberUtils.normalizeNumber(mobileNumber!!))
            }
            else -> {
                tietMobileNumber.error = getString(R.string.valid_mobile_number)
                viewModel.messageString.postValue(Resource.error(getString(R.string.valid_mobile_number)))
            }
        }
    }

    //Method to get forgot password response with phone from server
    private fun getForgotPasswordDetailsWithPhone(mobileNumber: String) {
        hideKeyboard()
        when {
            checkInternet() -> {
                showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                viewModel.getForgotPasswordWithPhone(mobileNumber)
            }
        }
    }

    @SuppressLint("observers")
    override fun setupObservers() {
        super.setupObservers()

        viewModel.forgotPasswordWithPhoneLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                if (!response.data.isNullOrEmpty()) {
                       showMessage( response.data!![0].otp)
                       showMessage( response.data!![0].resetKey)

                    startActivity(
                        OTPForgotPasswordActivity.getStartIntent(
                            context = this@ForgotPasswordWithPhoneActivity,
                            phoneNumber = mobileNumber!!,
                            otp = response.data!![0].otp,
                            resetKey = response.data!![0].resetKey
                        )
                    )
                } else {
                    Timber.d(response.settings?.message)
                }
            } else {
                   showMessage( response.settings!!.message)
                Timber.d(response.settings?.message)
            }
        })
    }
}