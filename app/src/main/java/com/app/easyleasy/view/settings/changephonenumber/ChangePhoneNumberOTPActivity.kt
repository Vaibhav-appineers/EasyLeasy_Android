package com.app.easyleasy.view.settings.changephonenumber

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.utility.validation.OTP_EMPTY
import com.app.easyleasy.utility.validation.OTP_INVALID
import com.app.easyleasy.R
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.extension.showSnackBar
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityChangePhoneNumberOtpBinding
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.HomeActivity
import com.app.easyleasy.viewModel.ChangePhoneNumberOTPViewModel
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants

class ChangePhoneNumberOTPActivity : BaseActivity<ChangePhoneNumberOTPViewModel>() {

    companion object {
        fun getStartIntent(context: Context, phoneNumber: String, otp: String): Intent {
            return Intent(context, ChangePhoneNumberOTPActivity::class.java).apply {
                putExtra("phoneNumber", phoneNumber)
                putExtra("otp", otp)
            }
        }
    }

    private var phoneNumber = ""
    var otp = ""
    private lateinit var binding: ActivityChangePhoneNumberOtpBinding

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_phone_number_otp)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-changePhoneNumberOtpScreen",
            "view_changePhoneNumberOtpScreen",
            "view_changePhoneNumberOtpScreen"
        )
        intent?.apply {
            phoneNumber = getStringExtra("phoneNumber") ?: ""
            otp = getStringExtra("otp") ?: ""
        }
        binding.apply {
            phoneNumber = "+1 " + this@ChangePhoneNumberOTPActivity.phoneNumber
            time = viewModel.getTimerValue()
            enableRetry = viewModel.getEnableRetrySetting()
            viewModel.startTimer()
            Toast.makeText(this@ChangePhoneNumberOTPActivity, otp, Toast.LENGTH_LONG).show()

            btnBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()
            }

            btnRetry.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Retry Button Click")
                viewModel.startTimer()
                showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                viewModel.callResendOTP(
                    type = "phone",
                    email = "",
                    userName = "",
                    phone = PhoneNumberUtils.normalizeNumber(this@ChangePhoneNumberOTPActivity.phoneNumber)
                )
            }

            btnValidate.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Validate Button Click")
                MSCGenerator.addAction(
                    GenConstants.ENTITY_USER,
                    GenConstants.ENTITY_APP,
                    "Validate OTP"
                )
                validateOTP()
            }
        }

        addObservers()
    }

    private fun addObservers() {
        viewModel.validationObserver.observe(this@ChangePhoneNumberOTPActivity, Observer {
            when (it.failType) {
                OTP_EMPTY -> {
                    showMessage(getString(R.string.alert_enter_otp))
                }
                OTP_INVALID -> {
                    showMessage(getString(R.string.alert_invalid_otp))
                }
            }
        })

        viewModel.otpPhoneNumberLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it.settings?.isSuccess == true && !it.data.isNullOrEmpty()) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP Validation success"
                )
                showMessage(it?.settings?.message!!)
                otp = it.data?.get(0)?.otp ?: ""
                Toast.makeText(this@ChangePhoneNumberOTPActivity, otp, Toast.LENGTH_LONG).show()
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this)
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP Validation failed"
                )
                showMessage(it?.settings?.message!!)
            }
        })

        viewModel.changePhoneNumberLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Change Phone number success"
                )
                viewModel.updatePhoneNumber(PhoneNumberUtils.normalizeNumber(this@ChangePhoneNumberOTPActivity.phoneNumber))
                showMessage(it.settings?.message!!)
                Handler(mainLooper).postDelayed({ navigateToSettingScreen() }, 3000L)
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this)
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Change Phone number failed"
                )
                showMessage(it.settings?.message!!)
            }
        })
    }

    /**
     * Verify entered OTP with received OTP
     */
    private fun validateOTP() {
        if (viewModel.isValid(
                otp = binding.otpView.text.toString(),
                sendOtp = otp
            ) && checkInternet()
        ) {
            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
            viewModel.callChangePhoneNumber(PhoneNumberUtils.normalizeNumber(this@ChangePhoneNumberOTPActivity.phoneNumber))
        }
    }

    /**
     * Navigate to setting screen
     */
    private fun navigateToSettingScreen() {
        val intent = Intent(this@ChangePhoneNumberOTPActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}