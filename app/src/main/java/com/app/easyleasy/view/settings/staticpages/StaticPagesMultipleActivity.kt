package com.app.easyleasy.view.settings.staticpages

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.R
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityStaticPagesMultipleBinding
import com.app.easyleasy.dataclasses.response.StaticPage
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.viewModel.StaticPagesViewModel
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants

class StaticPagesMultipleActivity : BaseActivity<StaticPagesViewModel>() {
    private var internetConnection: Boolean = false
    private var totalSize: Int = 0
    private var forceUpdate = false
    private var pageCodeList: ArrayList<StaticPage> = ArrayList()
    private var currentIndex: Int = 0
    private var iterationNeeded: Int = 0
    private lateinit var binding: ActivityStaticPagesMultipleBinding

    companion object {
        /**
         * Start intent to open StaticPagesMultipleActivity
         * @param mContext [ERROR : Context]
         * @param pageCode String Code of static page, which need to show
         * @return Intent
         */
        fun getStartIntent(
            mContext: Context,
            pageCodeList: ArrayList<StaticPage> = ArrayList()
        ): Intent {
            return Intent(mContext, StaticPagesMultipleActivity::class.java).apply {
                putExtra("page_code_list", pageCodeList)
            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_static_pages_multiple)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        addObserver()
        pageCodeList =
            intent?.getParcelableArrayListExtra<StaticPage>("page_code_list")
                ?: ArrayList<StaticPage>()
        binding.apply {
            srl.setOnRefreshListener {
                when {
                    checkInternet() -> initializeDataAndCallApi()
                }
            }
            tvStaticData.movementMethod = LinkMovementMethod.getInstance()
            tvStaticData.setLinkTextColor(
                ContextCompat.getColor(
                    this@StaticPagesMultipleActivity,
                    R.color.black
                )
            )

            ivBack.setOnClickListener { finish() }

            btnAgree.setOnClickListener {
                if (!pageCodeList[currentIndex].pageCode.isNullOrEmpty() && checkInternet()) {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Agree"
                    )
                    showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                    viewModel.callUpdateTNCPrivacyPolicy(pageCodeList[currentIndex].pageCode!!)
                } else {
                    hideProgressDialog()
                }
            }

        }

        /*Call Static Page Api*/
        if (!pageCodeList.isNullOrEmpty() && checkInternet()) {
            totalSize = pageCodeList.size
            if (totalSize == 2) {
                iterationNeeded = 2
                currentIndex = 0
            } else if (totalSize == 1) {
                iterationNeeded = 1
                currentIndex = 0
            }
            if (currentIndex < totalSize) {
                initializeDataAndCallApi()
            }

        } else {
            currentIndex = 0
            iterationNeeded = 0
            totalSize = 0
        }
    }

    private fun addObserver() {
        viewModel.staticPageResponseLiveData.observe(this, Observer {
            hideProgressDialog()
            binding.srl.isRefreshing = false
            when (it.settings?.isSuccess) {
                true -> {
                    if (!it.data.isNullOrEmpty()) {
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "Updated"
                        )
                        binding.tvScreenName.text = it.data!![0].pageTitle
                        binding.tvStaticData.text = it.data!![0].content?.getHtmlFormattedText()
                    } else {
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "Updated failed"
                        )
                        showMessage(it?.settings!!.message)
                        binding.btnAgree.visibility = View.INVISIBLE
                    }
                }
                false -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Updated failed"
                    )
                    showMessage(it?.settings!!.message)
                    binding.btnAgree.visibility = View.INVISIBLE
                }
                else -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Updated failed"
                    )
                    showMessage(it?.settings!!.message)
                    binding.btnAgree.visibility = View.INVISIBLE
                }
            }
        })

        viewModel.updateTNCResponseLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                if (totalSize > 1 && iterationNeeded == 2) {
                    currentIndex++
                    if (currentIndex < totalSize) {
                        initializeDataAndCallApi()
                    }
                    if (currentIndex == iterationNeeded) {
                        finish()
                    }
                } else {
                    finish()
                }

            } else {
                showMessage(it?.settings!!.message)
            }
        })

        viewModel.checkForInternetConnectionLiveData.observe(this, Observer { isInternetAvailable ->
            internetConnection = isInternetAvailable
        })
    }

    private fun initializeDataAndCallApi() {
        showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
        pageCodeList[currentIndex].forceUpdate?.let { forceUpdate = it }
        binding.ivBack.visibility = if (forceUpdate) View.INVISIBLE else View.VISIBLE
        binding.btnAgree.visibility = if (forceUpdate) View.VISIBLE else View.INVISIBLE
        if (!pageCodeList[currentIndex].pageCode.isNullOrEmpty()) {
            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
            viewModel.callStaticPage(pageCodeList[currentIndex].pageCode!!)
        } else {
           hideProgressDialog()

        }
    }

    /**
     * to get the html formatted text
     */
    fun String.getHtmlFormattedText(): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY);
        } else {
            Html.fromHtml(this)
        }
    }

    override fun onBackPressed() {
        if (!forceUpdate)
            super.onBackPressed()
    }
}