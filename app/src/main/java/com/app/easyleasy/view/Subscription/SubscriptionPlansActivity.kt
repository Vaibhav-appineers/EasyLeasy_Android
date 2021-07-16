package com.app.easyleasy.view.Subscription

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.SkuDetails


import com.app.easyleasy.R
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.RemoteIdlingResource
import com.app.easyleasy.commonUtils.utility.extension.getJsonDataFromAsset
import com.app.easyleasy.commonUtils.utility.extension.sharedPreference
import com.app.easyleasy.commonUtils.utility.extension.showSnackBar
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivitySubscriptionPlansBinding
import com.app.easyleasy.dataclasses.SubscriptionPlan
import com.app.easyleasy.dataclasses.response.GoogleReceipt
import com.app.easyleasy.dataclasses.response.StaticPage
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.settings.SettingsFragment
import com.app.easyleasy.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.easyleasy.viewModel.SettingsViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class SubscriptionPlansActivity : BaseActivity<SettingsViewModel>(), SubscriptionClickListener {


    lateinit var binding: ActivitySubscriptionPlansBinding
    private lateinit var subscriptionPlanListAdapter: SubscriptionViewAdapter
    //Showing nuber of colums in Subscription list
    private var numberOfColumns = 2
    //Subscription list
    private var subscriptionSKUList: List<SkuDetails> = arrayListOf()
    //In App Subscription list
    private var inAppSKUList: List<SkuDetails> = arrayListOf()
    // Selected subscription plan
    private var selectedSubscriptionPlan: SubscriptionPlan? = null
    // User Details of subscription
    private val user = sharedPreference.userDetail
    //Upgrade and downgrade Flag
    private var flag_upgrade_downgrade="0"
    //Purchase Subscription Token.
    private var purchaseToken = ""
    //Purchase Old Sku
    private var oldSku = ""


    companion object {
        fun getStartIntent(context: Context, check_upgrade_downgrade: String): Intent {
            return Intent(context, SubscriptionPlansActivity::class.java).apply {
                putExtra("check_upgrade_downgrade", check_upgrade_downgrade)
            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription_plans)
        binding.lifecycleOwner = this

        setFireBaseAnalyticsData(
            "id-subscriptionScreen",
            "view-subscriptionScreen",
            "view-subscriptionScreen"
        )


        intent?.apply {
            if (intent != null && intent.extras != null){
                if (intent.extras!!.containsKey(IConstants.CHECK_UPGRADE_DOWNGRADE)) {
                    flag_upgrade_downgrade = intent.extras?.getString(IConstants.CHECK_UPGRADE_DOWNGRADE)!!


                }}
        }
        initView()
    }


    /**
     * Initialize view
     */
    private fun initView() {
        initListeners()
        setBoldAndColorSpannable(
                binding.tvSubscriptionPolicyLinks,
                getString(R.string.terms_n_conditions),
                getString(R.string.eula_policy),
                getString(R.string.privacy_policy)
        )

        if (user?.subscription?.size!! > 0) {
            if (user?.subscription?.filter { it.subscriptionStatus == "1" }!!.size > 0) {
                viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
                initRecycleView()
                addObserver()
                loadSubscriptionPlans()
                binding.apply {
                    rvSubscriptionPlanList.visibility = View.VISIBLE
                    llNoData.visibility = View.GONE
                    premiumUserViewPager.visibility = View.GONE
                    btnSubscribe.visibility = View.VISIBLE
                    tvHeaderTitle.setText(getString(R.string.upgrade_and_downgrade))
                }

            } else {
                viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
                initRecycleView()
                addObserver()
                loadSubscriptionPlans()
                binding.rvSubscriptionPlanList.visibility = View.VISIBLE
                binding.llNoData.visibility = View.GONE
                binding.premiumUserViewPager.visibility = View.GONE
                binding.btnSubscribe.visibility = View.VISIBLE
            }
        } else {
            viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
            initRecycleView()
            addObserver()
            loadSubscriptionPlans()
            binding.rvSubscriptionPlanList.visibility = View.VISIBLE
            binding.llNoData.visibility = View.GONE
            binding.premiumUserViewPager.visibility = View.GONE
            binding.btnSubscribe.visibility = View.VISIBLE
        }
    }


/*
* load Subscription plans*/
    private fun loadSubscriptionPlans() {
        var subscriptionPlanList: ArrayList<SubscriptionPlan> = ArrayList()
        subscriptionPlanList =
                getJsonListDataFromAsset(this@SubscriptionPlansActivity, "subscription_plan_list.json")
        setSubscriptionListData(subscriptionPlanList)
    }
/*Subcrption plan json file parsing in array list*/
    private fun getJsonListDataFromAsset(
            context: Context,
            fileName: String
    ): ArrayList<SubscriptionPlan> {
        val jsonFileString = getJsonDataFromAsset(context, fileName)
        Log.i("data", jsonFileString.toString())
        val gson = Gson()
        val listReviewsType = object : TypeToken<ArrayList<SubscriptionPlan>>() {}.type
        var reviews: ArrayList<SubscriptionPlan> =
                gson.fromJson(jsonFileString, listReviewsType)
        reviews.forEachIndexed { idx, review -> Log.i("data", "> Item $idx:\n$review") }
        return reviews
    }

    /**
     * Initialize Recycleview for Subscription Plan
     */
    private fun initRecycleView() {
        subscriptionPlanListAdapter = SubscriptionViewAdapter(
               /* offerDate = user?.offerDate!!.toBoolean(),*/
                context = this@SubscriptionPlansActivity,
                list = ArrayList<SubscriptionPlan>(),
                subscriptionClickListener = this@SubscriptionPlansActivity
        )
        val layoutManager = GridLayoutManager(this@SubscriptionPlansActivity, numberOfColumns)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (subscriptionPlanListAdapter != null) {
                    when (subscriptionPlanListAdapter.getItemViewType(position)) {
                        1 -> 1
                        2 -> 2 //number of columns of the grid
                        else -> -1
                    }
                } else {
                    -1
                }
            }
        }
        binding.rvSubscriptionPlanList.layoutManager = layoutManager
        binding.rvSubscriptionPlanList.adapter = subscriptionPlanListAdapter
    }


    /**
     * Add Observers to listen changes in view model
     */
    private fun addObserver() {
        viewModel.subscriptionSKUList.observe(this, Observer {
            subscriptionSKUList = it
            if (subscriptionSKUList.isNotEmpty()) {
                subscriptionSKUList.forEach {
                    logger.dumpCustomEvent(
                            IConstants.EVENT_PURCHASED,
                            "Product Description: " + it.originalJson
                    )
                }
            }
        })

        viewModel.inAppSKUList.observe(this, Observer {
            inAppSKUList = it
            if (inAppSKUList.isNotEmpty()) {
                inAppSKUList.forEach {
                    logger.dumpCustomEvent(
                            IConstants.EVENT_PURCHASED,
                            "Product Description: " + it.originalJson
                    )
                }
            }
        })

        viewModel.orderReceiptJsonForOneTime.observe(this@SubscriptionPlansActivity, Observer {
            if (it.isNotEmpty()) {
                val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")
                navigateToHomeScreen()
            }
        })

        viewModel.orderReceiptJsonForUpgradeDowngradeSubscription.observe(this@SubscriptionPlansActivity, Observer {
            if (it.isNotEmpty()) {
                it.showSnackBar(this@SubscriptionPlansActivity, type = IConstants.SNAKBAR_TYPE_SUCCESS, duration = IConstants.SNAKE_BAR_SHOW_TIME_INT)
                Handler().postDelayed({  navigateToHomeScreen() }, IConstants.SNAKE_BAR_PROFILE_SHOW_TIME)
                mIdlingResource?.setIdleState(true)

            }
        })

        viewModel.orderReceiptJsonForSubscription.observe(this@SubscriptionPlansActivity, Observer {
            if (it.isNotEmpty()) {
                val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")
                if(!flag_upgrade_downgrade.equals("1")) {
                    viewModel.callBuySubscription(receiptData)
                }else{
                    flag_upgrade_downgrade="0"
                if (user?.subscription?.size!! > 0) {
                    if (user?.subscription?.filter { it.subscriptionStatus == "1" }!!.size > 0) {
                        for(index in user?.subscription!!.indices){
                            if(user?.subscription?.get(index)?.subscriptionStatus.equals("1")){
                        purchaseToken = user?.subscription?.get(index)?.purchaseToken!!
                        oldSku = user?.subscription?.get(index)?.productId!!
                        }}
                    }
                }
                }

            }
        })
        viewModel.buySubscriptionLiveData.observe(this@SubscriptionPlansActivity, Observer {
            if (it.settings?.isSuccess == true) {

                sharedPreference.userDetail?.subscription = it.data?.get(0)?.subscription

                navigateToHomeScreen()
            } else {
                it.settings?.message?.showSnackBar(this@SubscriptionPlansActivity, IConstants.SNAKBAR_TYPE_ERROR, duration = IConstants.SNAKE_BAR_SHOW_TIME_INT)

            }
        })


    }

    /**
     * Set Entertainment List Data
     * @param it ArrayList<Entertainment>
     */
    private fun setSubscriptionListData(it: ArrayList<SubscriptionPlan>) {
        if (it.isNotEmpty()) {
            showData()
        } else {
            showNoData()
        }
        subscriptionPlanListAdapter.list = it
        subscriptionPlanListAdapter.notifyDataSetChanged()
    }

    private fun showData() {
        binding.rvSubscriptionPlanList.visibility = View.VISIBLE
        binding.llNoData.visibility = View.GONE
    }

    private fun showNoData() {
        binding.rvSubscriptionPlanList.visibility = View.GONE
        binding.llNoData.visibility = View.VISIBLE
    }

    /**
     * Initialise listeners to listen all click and other actions performed by user
     */
    private fun initListeners() {
        binding.apply {
            ivBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()
            }


            btnSubscribe.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Subscribe Button Click")

                if (subscriptionSKUList.isNotEmpty() && selectedSubscriptionPlan != null) {
                    if (selectedSubscriptionPlan!!.planValidityInDays == "0" || selectedSubscriptionPlan!!.planValidityInDays == "7") {
                        inAppSKUList.forEach {
                            when (selectedSubscriptionPlan!!.planValidityInDays) {
                                "0" -> {
                                    if (it.sku == "com.app.reboundapp.life_time") {

                                        viewModel.makePurchase(
                                                this@SubscriptionPlansActivity,
                                                skuDetails = it, purchaseToken = purchaseToken,oldSku=oldSku
                                        )
                                    }
                                }
                                "7" -> {
                                    if (it.sku == "com.app.reboundapp.seven_days") {
                                        viewModel.makePurchase(
                                                this@SubscriptionPlansActivity,
                                                skuDetails = it, purchaseToken = purchaseToken,oldSku=oldSku
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        subscriptionSKUList.forEach {
                            when (selectedSubscriptionPlan!!.planValidityInDays) {
                                "30" -> {
                                    if (it.sku == "com.app.easyleasy.monthly") {
                                        viewModel.makePurchase(
                                                this@SubscriptionPlansActivity,
                                                skuDetails = it, purchaseToken = purchaseToken,oldSku=oldSku
                                        )
                                    }
                                }
                                "90" -> {
                                    if (it.sku == "com.app.easyleasy.3months") {
                                        viewModel.makePurchase(
                                                this@SubscriptionPlansActivity,
                                                skuDetails = it, purchaseToken = purchaseToken,oldSku=oldSku
                                        )
                                    }
                                }
                                "180" -> {
                                    if (it.sku == "com.app.easyleasy.6months") {
                                        viewModel.makePurchase(
                                                this@SubscriptionPlansActivity,
                                                skuDetails = it, purchaseToken = purchaseToken,oldSku=oldSku
                                        )
                                    }
                                }
                                "365" -> {
                                    if (it.sku == "yearly") {
                                        viewModel.makePurchase(
                                                this@SubscriptionPlansActivity,
                                                skuDetails = it, purchaseToken = purchaseToken,oldSku=oldSku
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Set spannable text for TNC and Privacy Policy
     * @param textView TextView
     * @param portions Array<out String>
     */
    private fun setBoldAndColorSpannable(textView: TextView, vararg portions: String) {
        val label = textView.text.toString()
        val spannableString1 = SpannableString(label)
        for (portion in portions) {
            val startIndex = label.indexOf(portion)
            val endIndex = startIndex + portion.length
            try {
                if (portion.equals(getString(R.string.terms_n_conditions), true))
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: java.util.ArrayList<StaticPage> =
                                    java.util.ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsFragment.STATIC_PAGE_TERMS_CONDITION,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscriptionPlansActivity,
                                        pageCodeList
                                    )
                                startActivity(intent)
                            } else {
                                showMessage(
                                    getString(R.string.network_connection_error)
                                )

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                else if (portion.equals(getString(R.string.privacy_policy), true)) {
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: java.util.ArrayList<StaticPage> =
                                    java.util.ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsFragment.STATIC_PAGE_PRIVACY_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscriptionPlansActivity,
                                        pageCodeList
                                    )
                                startActivity(intent)
                            } else {
                                showMessage(
                                    getString(R.string.network_connection_error)
                                )

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }else if (portion.equals(getString(R.string.eula_policy), true)) {
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: java.util.ArrayList<StaticPage> =
                                    java.util.ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsFragment.STATIC_PAGE_EULA_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscriptionPlansActivity,
                                        pageCodeList
                                    )
                                startActivity(intent)
                            } else {
                                showMessage(
                                    getString(R.string.network_connection_error)
                                )

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
                spannableString1.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@SubscriptionPlansActivity,
                            R.color.colorPrimary
                        )
                    ), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                spannableString1.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                textView.movementMethod = LinkMovementMethod.getInstance()
                textView.highlightColor = Color.TRANSPARENT
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        textView.text = spannableString1
    }

    override fun onSubscriptionClick(data: SubscriptionPlan) {
        selectedSubscriptionPlan = data
    }

    @Nullable
    private var mIdlingResource: RemoteIdlingResource? = null

    /**
     * Only called from test, creates and returns a new [RemoteIdlingResource].
     */
    @VisibleForTesting
    @NonNull
    fun getIdlingResource(): RemoteIdlingResource {
        if (mIdlingResource == null) {
            mIdlingResource = RemoteIdlingResource()
        }

        return mIdlingResource as RemoteIdlingResource
    }





    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {

    }

}