package com.app.easyleasy.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.app.easyleasy.databinding.ActivitySplashBinding
import com.app.easyleasy.view.onboarding.OnBoardingActivity
import com.app.easyleasy.viewModel.HomeViewModel
import com.hb.logger.Logger

@Suppress("DEPRECATION")
class SplashActivity : BaseActivity<HomeViewModel>() {
    var dataBinding: ActivitySplashBinding? = null

    override fun setDataBindingLayout() {
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        dataBinding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)

    }

    override fun setupView(savedInstanceState: Bundle?) {
        Handler().postDelayed({
            finish()
            startActivity(Intent(this@SplashActivity, getLaunchClass()))
        }, 500)
    }

    /**
     * Choose activity to open
     *
     * @return if user already login, then open home activity, else open login activity
     */
    private fun getLaunchClass(): Class<*> {
        return if (sharedPreference.isLogin) {
            // Return Home activity
            Logger.setUserInfo(sharedPreference.userDetail?.email ?: "")
            HomeActivity::class.java
        } else {
            //Return Login Activity
            if (sharedPreference.isOnBoardingShown) {
                (application as AppineersApplication).getLoginActivity()
            } else {
                OnBoardingActivity::class.java
            }
        }
    }

}