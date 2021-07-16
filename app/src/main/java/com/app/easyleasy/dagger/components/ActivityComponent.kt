package com.app.easyleasy.dagger.components

import com.app.easyleasy.dagger.ActivityScope
import com.app.easyleasy.dagger.modules.ActivityModule
import com.app.easyleasy.view.*
import com.app.easyleasy.view.Subscription.SubscribedUserActivity
import com.app.easyleasy.view.Subscription.SubscriptionPlansActivity
import com.app.easyleasy.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.easyleasy.view.authentication.resetpassword.ResetPasswordActivity
import com.app.easyleasy.view.authentication.forgotpassword.email.ForgotPasswordWithEmailActivity
import com.app.easyleasy.view.authentication.forgotpassword.phone.ForgotPasswordWithPhoneActivity
import com.app.easyleasy.view.onboarding.OnBoardingActivity
import com.app.easyleasy.view.settings.feedback.SendFeedbackActivity
import com.app.easyleasy.view.settings.changepassword.ChangePasswordActivity
import com.app.easyleasy.view.settings.editprofile.EditProfileActivity

import com.app.easyleasy.view.settings.changephonenumber.ChangePhoneNumberActivity
import com.app.easyleasy.view.settings.changephonenumber.ChangePhoneNumberOTPActivity
import dagger.Component
import com.app.easyleasy.view.authentication.login.loginwithemail.LoginWithEmailActivity
import com.app.easyleasy.view.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity
import com.app.easyleasy.view.authentication.login.loginwithphonenumber.LoginWithPhoneNumberActivity
import com.app.easyleasy.view.authentication.login.loginwithphonenumbersocial.LoginWithPhoneNumberSocialActivity
import com.app.easyleasy.view.authentication.otp.otpsignup.OTPSignUpActivity
import com.app.easyleasy.view.authentication.signup.SignUpActivity
import com.app.easyleasy.view.authentication.otp.otpforgotpassword.OTPForgotPasswordActivity
@ActivityScope
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [ActivityModule::class]
)
interface ActivityComponent{
    fun inject(splashActivity: SplashActivity)
    fun inject(homeActivity: HomeActivity)
    fun inject(staticPagesMultipleActivity: StaticPagesMultipleActivity)
    fun inject(forgotPasswordWithPhoneActivity: ForgotPasswordWithPhoneActivity)
    fun inject(forgotPasswordWithEmailActivity: ForgotPasswordWithEmailActivity)
    fun inject(resetPasswordActivity: ResetPasswordActivity)
    fun inject(signUpActivity: SignUpActivity)
    fun inject(otpSignUpActivity: OTPSignUpActivity)
    fun inject(loginWithPhoneNumberActivity: LoginWithPhoneNumberActivity)
    fun inject(loginWithEmailSocialActivity: LoginWithEmailSocialActivity)
    fun inject(loginWithEmailActivity: LoginWithEmailActivity)
    fun inject(loginWithPhoneNumberSocialActivity: LoginWithPhoneNumberSocialActivity)
    fun inject(sendFeedbackActivity: SendFeedbackActivity)
    fun inject(changePasswordActivity: ChangePasswordActivity)
    fun inject(changePhoneNumberActivity: ChangePhoneNumberActivity)
    fun inject(onBoardingActivity: OnBoardingActivity)
    fun inject(otpForgotPasswordActivity: OTPForgotPasswordActivity)
    fun inject(navigationSideMenu: NavigationSideMenu)
    fun inject(changePhoneNumberOTPActivity: ChangePhoneNumberOTPActivity)
    fun inject(editProfileActivity: EditProfileActivity)
    fun inject(SubscriptionPlansActivity: SubscriptionPlansActivity)
    fun inject(SubscribedUserActivity: SubscribedUserActivity)
}