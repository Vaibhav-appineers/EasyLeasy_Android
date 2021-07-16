package com.app.easyleasy.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.extension.circularLoadImage
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityNavigationSideMenuBinding
import com.app.easyleasy.databinding.AppBarMainBinding
import com.app.easyleasy.databinding.ContentMainBinding
import com.app.easyleasy.databinding.NavHeaderMainBinding
import com.app.easyleasy.dataclasses.response.LoginResponse
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.friends.FriendsFragment
import com.app.easyleasy.view.gallery.GalleryPagerActivity
import com.app.easyleasy.view.home.HomeFragment
import com.app.easyleasy.view.message.MessagesFragment
import com.app.easyleasy.view.profile.ProfileFragment
import com.app.easyleasy.view.settings.SettingsFragment
import com.app.easyleasy.viewModel.HomeViewModel

class NavigationSideMenu : BaseActivity<HomeViewModel>() {
    private lateinit var binding: ActivityNavigationSideMenuBinding
    private lateinit var navHeaderBind: NavHeaderMainBinding
    private lateinit var appBarLayout: AppBarMainBinding
    private lateinit var contentMainLayout: ContentMainBinding
    var navigationPosition: Int = 0
    lateinit var user: LoginResponse

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_navigation_side_menu)
        binding.lifecycleOwner = this

        appBarLayout = binding.appBarMain
        contentMainLayout = binding.appBarMain.contentMain

        navHeaderBind = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.nav_header_main,
            binding.navigationView,
            false
        )
        binding.navigationView.addHeaderView(navHeaderBind.root)
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-navigationScreen",
            "view_navigationScreen",
            "view_navigationScreen"
        )

        binding.apply {
            setSupportActionBar(binding.appBarMain.toolbar)
            setUpDrawerLayout()

            navigationPosition = R.id.action_home
            setCurrentFragment(HomeFragment())
            navigationView.setCheckedItem(navigationPosition)
            binding.appBarMain.toolbar.title = getString(R.string.home)

            setupNavigationDrawerProfile()

            navigationView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_home -> {
                        binding.appBarMain.toolbar.title = getString(R.string.home)
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is HomeFragment) {
                            true
                        } else {
                            setCurrentFragment(HomeFragment())
                            true
                        }
                    }
                    R.id.action_friends -> {
                        binding.appBarMain.toolbar.title = getString(R.string.friends)
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is FriendsFragment) {
                            true
                        } else {
                            setCurrentFragment(FriendsFragment())
                            true
                        }
                    }
                    R.id.action_message -> {
                        binding.appBarMain.toolbar.title = getString(R.string.message)
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is MessagesFragment) {
                            true
                        } else {
                            setCurrentFragment(MessagesFragment())
                            true
                        }
                    }
                    R.id.action_profile -> {
                        binding.appBarMain.toolbar.title = getString(R.string.profile)
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is ProfileFragment) {
                            true
                        } else {
                            setCurrentFragment(ProfileFragment())
                            true
                        }
                    }
                    R.id.action_settings -> {
                        binding.appBarMain.toolbar.title = getString(R.string.setting)
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is SettingsFragment) {
                            true
                        } else {
                            setCurrentFragment(SettingsFragment())
                            true
                        }
                    }
                }
                // set item as selected to persist highlight
                menuItem.isChecked = true
                // close drawer when item is tapped
                drawerLayout.closeDrawers()
                true
            }



            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(p0: Int) {
                }

                override fun onDrawerSlide(p0: View, p1: Float) {
                }

                override fun onDrawerClosed(p0: View) {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Drawer closed")
                }

                override fun onDrawerOpened(p0: View) {
                    hideKeyboard()
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Drawer opened")
                }
            })
        }

        navHeaderBind.ivUserProfile.setOnClickListener {
            logger.dumpCustomEvent(
                IConstants.EVENT_CLICK,
                "Click on image to show profile image in full screen"
            )
            startActivity(
                GalleryPagerActivity.getStartIntent(
                    this@NavigationSideMenu, arrayListOf(
                        sharedPreference.userDetail?.profileImage
                            ?: ""
                    ), 0
                )
            )
        }
    }

    private fun setupNavigationDrawerProfile() {
        user = sharedPreference.userDetail!!
        val userAddress = """${user.address}${", " + user.city}${", " + user.stateName}"""
        val userName = """${user.firstName}${" " + user.lastName}"""
        navHeaderBind.tvTitle.text = userName
        navHeaderBind.ivUserProfile.circularLoadImage(user.profileImage, R.drawable.user_profile)
        navHeaderBind.tvSubTitle.text = user.email
    }

    private fun setUpDrawerLayout() {
        val toggle = ActionBarDrawerToggle(
            this,  binding.drawerLayout,  appBarLayout.toolbar, R.string.drawerOpen, R.string.drawerClose
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)
    }


    @SuppressLint("WrongConstant")
    override fun onBackPressed() {

        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.closeDrawer(Gravity.START)
            return
        }

        when (navigationPosition) {
            R.id.action_home -> {
                //super.onBackPressed()
                finish()
            }
            else -> {
                navigationPosition = R.id.action_home
                setCurrentFragment(HomeFragment())
                binding.navigationView.setCheckedItem(navigationPosition)
                binding.appBarMain.toolbar.title = getString(R.string.home)
            }
        }
    }


    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameContainer, fragment)
            commit()
        }


}