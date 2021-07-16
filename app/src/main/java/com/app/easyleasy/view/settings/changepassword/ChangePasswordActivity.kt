package com.app.easyleasy.view.settings.changepassword

import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.utility.validation.*
import com.app.easyleasy.R
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.extension.focusOnField
import com.app.easyleasy.commonUtils.utility.extension.getTrimText
import com.app.easyleasy.commonUtils.utility.extension.hideKeyBoard
import com.app.easyleasy.commonUtils.utility.extension.showSnackBar
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityChangePasswordBinding
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.viewModel.ChangePasswordViewModel
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants

class ChangePasswordActivity : BaseActivity<ChangePasswordViewModel>() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-changePasswordScreen","view_changePasswordScreen","view_changePasswordScreen")
        binding.apply {
            ibtnBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()
            }
            btnUpdate.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Update Button Click")
                MSCGenerator.addAction(GenConstants.ENTITY_USER, GenConstants.ENTITY_APP, "Update Button Click")
                changePassword()
            }
        }

        addObservers()
    }

    private fun addObservers() {

        viewModel.validationObserver.observe(this@ChangePasswordActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when (it.failType) {
                OLD_PASSWORD_EMPTY -> {
                       showMessage( getString(R.string.alert_enter_old_password))
                }
                PASSWORD_EMPTY -> {
                       showMessage( getString(R.string.alert_enter_new_password))
                }
                PASSWORD_INVALID -> {
                       showMessage( getString(R.string.alert_valid_password))
                }
                CONFORM_PASSWORD_EMPTY -> {
                       showMessage( getString(R.string.alert_enter_confirm_password))
                }
                PASSWORD_NOT_MATCH -> {
                       showMessage( getString(R.string.alert_confirm_password_not_match))
                }

                OLD_NEW_PASSWORD_MATCH -> {
                    showMessage( getString(R.string.alert_password_old_new_match))
                }
            }
        })

        viewModel.changePasswordLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                MSCGenerator.addAction(GenConstants.ENTITY_APP, GenConstants.ENTITY_USER, "Change Password Success")
                showMessage(it.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                Handler(mainLooper).postDelayed({ finish() }, 3000L)
            }  else if (!handleApiError(it.settings)) {
                MSCGenerator.addAction(GenConstants.ENTITY_APP, GenConstants.ENTITY_USER, "Change Password Failed")
                it?.settings?.message?.showSnackBar(this)
            }else {
                MSCGenerator.addAction(GenConstants.ENTITY_APP, GenConstants.ENTITY_USER, "Change Password Failed")
                showMessage(it.settings!!.message)
            }
        })
    }

    /**
     * Perform change password.
     * Check Internet connection and validation of input fields.
     * If all is ok, then all api to change password
     */
    private fun changePassword() {
        binding.root.hideKeyBoard()
        if (viewModel.isValid(
                oldPassword = binding.tietOldPassword.getTrimText(),
                newPassword = binding.tietNewPassword.getTrimText(),
                confirmPassword = binding.tietConfirmPassword.getTrimText()
            )
        ) {
            if (checkInternet()) {
                showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                viewModel.callChangePassword(
                    oldPassword = binding.tietOldPassword.getTrimText(),
                    newPassword = binding.tietNewPassword.getTrimText()
                )
            }
        }
    }

}