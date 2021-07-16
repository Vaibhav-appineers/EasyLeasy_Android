package com.app.easyleasy.view.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.view.gallery.GalleryPagerActivity
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.extension.sharedPreference
import com.app.easyleasy.dagger.components.FragmentComponent
import com.app.easyleasy.databinding.FragmentProfileBinding
import com.app.easyleasy.mvvm.AppConfig
import com.app.easyleasy.mvvm.BaseFragment
import com.app.easyleasy.view.settings.editprofile.EditProfileActivity
import com.app.easyleasy.viewModel.UserProfileViewModel

class ProfileFragment :  BaseFragment<UserProfileViewModel>() {

    companion object {
        const val FRAGMENT_TAG = "ProfileFragment"
    }

    private lateinit var binding: FragmentProfileBinding

    override fun provideLayoutId(): Int {
        return R.layout.fragment_profile
    }

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun setupView(view: View) {
        setFireBaseAnalyticsData("id-profileScreen","view_profileScreen","view_profileScreen")
        binding = DataBindingUtil.bind(view)!!
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        if (AppConfig.AdProvider_MoPub) {
            this.activity?.let { showBannerAd(it, binding.moPubAdView) }
        } else {
            this.activity?.let { showBannerAd(it, binding.adView) }
        }
        //val user= LoginResponse(profileImage="",firstName = "Mahesh",lastName = "Lipane",userName = "maheshl@theappineers.com")
        //sharedPreference.userDetail=user
        binding.user = sharedPreference.userDetail
        binding.apply {
            btnEdit.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Edit Button Click")
                startActivity(Intent(activity, EditProfileActivity::class.java))
            }

            sivUserImage.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Click on image to show profile image in full screen")
                startActivity(
                    GalleryPagerActivity.getStartIntent(
                        activity!!, arrayListOf(sharedPreference.userDetail?.profileImage
                    ?: "")
                    , 0))
            }
        }
    }

    override fun setDataBindingLayout() {

    }

    override fun setupObservers() {
        super.setupObservers()

        (activity?.application as AppineersApplication).isProfileUpdated.observe(this, Observer {
            binding.user = sharedPreference.userDetail
            binding.executePendingBindings()
        })

        viewModel.updateUserLiveData.observe(this, Observer{
            if (it.settings?.isSuccess == true) {
                binding.user = sharedPreference.userDetail
                binding.executePendingBindings()
            }else{
                showMessage( it.settings!!.message)
            }
        })

        (activity?.application as AppineersApplication).isAdRemoved.observe(this@ProfileFragment, Observer {
            if(it){
                binding.adView.visibility = View.GONE
            }
        })
    }

}