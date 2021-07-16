package com.app.easyleasy.view.settings.changephonenumber

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.utility.validation.PHONE_NUMBER_EMPTY
import com.app.easyleasy.utility.validation.PHONE_NUMBER_INVALID
import com.app.easyleasy.utility.validation.PHONE_NUMBER_INVALID_LENGHT
import com.app.easyleasy.R
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.extension.focusOnField
import com.app.easyleasy.commonUtils.utility.extension.getTrimText
import com.app.easyleasy.commonUtils.utility.extension.hideKeyBoard
import com.app.easyleasy.commonUtils.utility.extension.showSnackBar
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityChangePhoneNumberBinding
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.viewModel.ChangePhoneNumberViewModel
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants

class ChangePhoneNumberActivity : BaseActivity<ChangePhoneNumberViewModel>() {
    private lateinit var binding: ActivityChangePhoneNumberBinding

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_phone_number)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        binding.apply {
            ibtnBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()
            }
            btnSend.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Send Button Click")
                MSCGenerator.addAction(GenConstants.ENTITY_USER, GenConstants.ENTITY_APP, "Generate OTP")
                callCheckUnique(binding.tietNewPhoneNumber.getTrimText())
            }

            tietNewPhoneNumber.addTextChangedListener(
                PhoneNumberFormattingTextWatcher("US")
            )

            tietNewPhoneNumber.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                binding.tietNewPhoneNumber.hideKeyBoard()
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Send Button Click")
                    callCheckUnique(binding.tietNewPhoneNumber.getTrimText())
                    return@OnEditorActionListener true
                }
                false
            })
        }

        addObserver()
    }

    private fun addObserver() {
        viewModel.validationObserver.observe(this, Observer {
            binding.root.focusOnField(it.failedViewId)
            when (it.failType) {
                PHONE_NUMBER_EMPTY -> {
                    showMessage(getString(R.string.alert_enter_mobile_number))
                }
                PHONE_NUMBER_INVALID -> {
                    showMessage(getString(R.string.alert_invalid_phone_number_format))
                }
                PHONE_NUMBER_INVALID_LENGHT -> {
                    showMessage(getString(R.string.alert_invalid_phone_number))
                }
            }
        })

        viewModel.changePhoneNumberLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it.settings?.isSuccess == true && !it.data.isNullOrEmpty()) {
                MSCGenerator.addAction(GenConstants.ENTITY_APP, GenConstants.ENTITY_USER, "OTP generated")
                showMessage(it.settings!!.message)
                startActivity(
                    ChangePhoneNumberOTPActivity.getStartIntent(
                        context = this@ChangePhoneNumberActivity,
                        phoneNumber = binding.tietNewPhoneNumber.getTrimText(),
                        otp = it.data?.get(0)?.otp ?: ""
                    )
                )
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this)
            } else {
                MSCGenerator.addAction(GenConstants.ENTITY_APP, GenConstants.ENTITY_USER, "OTP generation failed")
                showMessage(it.settings!!.message)
            }
        })
    }

    private fun callCheckUnique(number: String) {
        if (viewModel.isValid(number) && checkInternet()) {
            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
            viewModel.checkUniqueUser("phone", "", number, "")
        }
    }
}