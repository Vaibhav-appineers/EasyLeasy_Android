package com.app.easyleasy.view.settings

import android.content.Intent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.android.billingclient.api.SkuDetails
import com.app.easyleasy.BuildConfig
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.app.easyleasy.commonUtils.common.CommonUtils.Companion.openAppNotificationSettings
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.IConstants.Companion.EMPTY_LOADING_MSG
import com.app.easyleasy.commonUtils.utility.NotificationUtils
import com.app.easyleasy.commonUtils.utility.dialog.DialogUtil
import com.app.easyleasy.commonUtils.utility.dialog.RateUsDialog
import com.app.easyleasy.commonUtils.utility.extension.showSnackBar
import com.app.easyleasy.dagger.components.FragmentComponent
import com.app.easyleasy.databinding.FragmentSettingsBinding
import com.app.easyleasy.dataclasses.response.StaticPage
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.mvvm.BaseFragment
import com.app.easyleasy.view.Subscription.SubscribedUserActivity
import com.app.easyleasy.view.Subscription.SubscriptionPlansActivity
import com.app.easyleasy.view.settings.feedback.SendFeedbackActivity
import com.app.easyleasy.view.settings.changepassword.ChangePasswordActivity
import com.app.easyleasy.view.settings.changephonenumber.ChangePhoneNumberActivity
import com.app.easyleasy.view.settings.editprofile.EditProfileActivity
import com.app.easyleasy.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.easyleasy.viewModel.SettingsViewModel
import com.hb.logger.Logger
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment<SettingsViewModel>() {

    companion object {
        const val STATIC_PAGE_ABOUT_US: String = "aboutus"
        const val STATIC_PAGE_TERMS_CONDITION: String = "termsconditions"
        const val STATIC_PAGE_PRIVACY_POLICY: String = "privacypolicy"
        const val STATIC_PAGE_EULA_POLICY: String = "eula"
    }

    private var binding: FragmentSettingsBinding? = null
    private var addFreeSKU: SkuDetails? = null

    override fun provideLayoutId(): Int {
        return R.layout.fragment_settings
    }

    override fun injectDependencies(fragmentComponent:       FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupView(view: View) {
        binding = DataBindingUtil.bind(view)
        binding?.tvLogs?.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        setFireBaseAnalyticsData("id-settingsscreen", "view_settingsscreen", "view_settingsscreen")
        binding?.viewSetting = viewModel.settingConfig
        binding.apply {
            tbPushNotification.setOnClickListener {
                logger.dumpCustomEvent(
                    IConstants.EVENT_CLICK,
                    "Push Notification Setting Button Click"
                )
                confirmOpenNotificationSettings()
            }
            tvGoAddFree.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Free Button Click")
                setFireBaseAnalyticsData(
                    "id-goadfree",
                    "click_goadfreeclick",
                    "click_goadfreeclick"
                )
                when {
                    checkInternet() -> viewModel.makePurchase(activity, skuDetails = addFreeSKU)
                }

            }
            tvSubscription.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Free Button Click")
                setFireBaseAnalyticsData(
                    "id-subcription",
                    "click_subcriptionclick",
                    "click_subcriptionclick"
                )
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Try Premium Button Click")
                val user = sharedPreference.userDetail
                if (user?.subscription?.size!!>0) {
                    if ( user?.subscription?.filter { it.subscriptionStatus=="1" }!!.size>0) {
                        startActivity(
                            Intent(
                                activity,
                                SubscribedUserActivity::class.java
                            )
                        )
                    } else {
                        startActivity(
                            Intent(
                                activity,
                                SubscriptionPlansActivity::class.java
                            )
                        )
                    }
                } else {
                    startActivity(
                        Intent(
                            activity,
                            SubscriptionPlansActivity::class.java
                        )
                    )
                }

            }
            tvShareApp.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Share App Button Click")
                openShareIntent()
            }

            tvEditProfile.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Edit Profile Button Click")
                startActivity(Intent(activity, EditProfileActivity::class.java))
            }

            tvChangePassword.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Change Password Button Click")
                startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
            }

            tvRateApp.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Rate App Button Click")
                RateUsDialog(requireActivity()).show(
                    requireActivity().supportFragmentManager,
                    "Rating"
                )
            }

            tvDeleteAccount.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Delete Button Click")
                performDeleteAccount()
            }

            tvLogout.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Log out Button Click")
                performLogOut()
            }

            tvChangePhoneNumber.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Change Phone Number Button Click")
                startActivity(Intent(requireContext(), ChangePhoneNumberActivity::class.java))
            }

            tvSendFeedback.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Report Problem Button Click")
                startActivity(Intent(requireContext(), SendFeedbackActivity::class.java))
            }

            tvAboutUs.setOnClickListener {
                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                pageCodeList.add(
                    StaticPage(
                        pageCode = STATIC_PAGE_ABOUT_US,
                        forceUpdate = false
                    )
                )
                val intent =
                    StaticPagesMultipleActivity.getStartIntent(requireContext(), pageCodeList)
                startActivity(intent)

            }

            tvPrivacyPolicy.setOnClickListener {
                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                pageCodeList.add(
                    StaticPage(
                        pageCode = STATIC_PAGE_PRIVACY_POLICY,
                        forceUpdate = false
                    )
                )
                val intent =
                    StaticPagesMultipleActivity.getStartIntent(requireContext(), pageCodeList)
                startActivity(intent)

            }

            tvLogs.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Report Problem Button Click")
                Logger.launchActivity()
            }

            tvTermsCondition.setOnClickListener {
                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                pageCodeList.add(
                    StaticPage(
                        pageCode = STATIC_PAGE_TERMS_CONDITION,
                        forceUpdate = false
                    )
                )
                val intent =
                    StaticPagesMultipleActivity.getStartIntent(requireContext(), pageCodeList)
                startActivity(intent)

            }
        }

        addObservers()
    }

    override fun setDataBindingLayout() {}

    private fun addObservers() {
        viewModel.logoutLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                viewModel.performLogout()
                navigateToLoginScreen()
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(activity)
            } else {
                showMessage(it.settings?.message!!)
            }
        })

        viewModel.deleteAccountLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                viewModel.performLogout()
                navigateToLoginScreen()
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(activity)
            } else {
                showMessage(it.settings?.message!!)
            }
        })


        viewModel.addFreeSKU.observe(requireActivity(), Observer {
            addFreeSKU = it
        })

        viewModel.orderReceiptJson.observe(activity as BaseActivity<*>, Observer {
            if (it.isNotEmpty()) {
                sharedPreference.isAdRemoved = true
                viewModel.settingConfig.showRemoveAdd = false
                binding?.viewSetting = viewModel.settingConfig
                binding?.executePendingBindings()

                viewModel.callGoAdFree(it)
                (AppineersApplication()).isAdRemoved.value = true
            }
        })


        (activity?.application as AppineersApplication).isLogStatusUpdated.observe(
            this@SettingsFragment,
            Observer {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = EMPTY_LOADING_MSG
                )
                showOrHideLogOption()
            })
    }


    /**
     * Navigate user to Login Screen if user not logged in or want to logout
     */
    private fun navigateToLoginScreen() {
        sharedPreference.userDetail = null
        sharedPreference.isLogin = false
        sharedPreference.authToken = ""
        Logger.setUserInfo("")
        val intent = Intent(
            requireContext(),
            (activity?.application as AppineersApplication).getLoginActivity()
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun showOrHideLogOption() {
        if (!BuildConfig.DEBUG) {
            if (sharedPreference.logStatusUpdated.equals("inactive", true)) {
                binding?.tvLogs?.visibility = View.GONE
            } else if (sharedPreference.logStatusUpdated.equals("active", true)) {
                binding?.tvLogs?.visibility = View.VISIBLE
            }
        }
        hideProgressDialog()
    }

    /**
     * Show confirmation message to delete account
     */
    private fun performDeleteAccount() {
        DialogUtil.alert(
            context = requireContext(),
            msg = getString(R.string.delete_account_alert),
            positiveBtnText = getString(R.string.label_yes_button),
            negativeBtnText = getString(R.string.label_no_button),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    //mBaseActivity?.setFireBaseAnalyticsData("id-deleteaccount", "click_deleteaccount", "click_deleteaccount")
                    when {
                        checkInternet() -> {
                            showProgressDialog(
                                isCheckNetwork = true,
                                isSetTitle = false,
                                title = EMPTY_LOADING_MSG
                            )
                            viewModel.callDeleteAccount()
                        }
                    }
                }

                override fun onCancel(isNeutral: Boolean) {}
            },
            isCancelable = false
        )
    }

    /**
     * Show confirmation message to log out
     */
    private fun performLogOut() {
        DialogUtil.alert(
            context = requireContext(), msg = getString(R.string.logout_alert),
            positiveBtnText = getString(R.string.ok), negativeBtnText = getString(R.string.cancel),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    when {
                        checkInternet() -> {
                            showProgressDialog(
                                isCheckNetwork = true,
                                isSetTitle = false,
                                title = EMPTY_LOADING_MSG
                            )
                            viewModel.callLogout()
                        }
                    }

                }

                override fun onCancel(isNeutral: Boolean) {}
            }, isCancelable = false
        )
    }

    /**
     * Share application
     */
    private fun openShareIntent() {
        //setFireBaseAnalyticsData("id-shareapp", "click_shareapp", "click_shareapp")
        val shareBody =
            "Try The Appineers App on Playstore:\nhttps://play.google.com/store/apps/details?id=${context?.packageName}"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            "Download ${context?.getString(R.string.app_name)}"
        )
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(
            Intent.createChooser(
                sharingIntent,
                resources.getString(R.string.share_using)
            )
        )
    }

    /**
     * Show confirmation message to opening system notfication settings
     */
    private fun confirmOpenNotificationSettings() {
        DialogUtil.alert(
            context = requireActivity(),
            msg = getString(R.string.open_notification_settings_alert),
            positiveBtnText = getString(R.string.ok),
            negativeBtnText = getString(R.string.cancel),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    openAppNotificationSettings(requireActivity())
                }

                override fun onCancel(isNeutral: Boolean) {
                    binding?.tbPushNotification?.isChecked =
                        NotificationUtils.areNotificationsEnabled(
                            mContext = requireContext(),
                            application = requireActivity().application as AppineersApplication
                        )
                }
            },
            isCancelable = false
        )
    }

    override fun onResume() {
        super.onResume()
        binding?.tbPushNotification?.isChecked = NotificationUtils.areNotificationsEnabled(
            mContext = requireContext(),
            application = requireActivity().application as AppineersApplication
        )
    }
}