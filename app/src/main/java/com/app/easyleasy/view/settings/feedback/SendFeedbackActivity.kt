package com.app.easyleasy.view.settings.feedback

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.easyleasy.utility.validation.FEEDBACK_EMPTY
import com.app.easyleasy.BuildConfig
import com.app.easyleasy.R
import com.app.easyleasy.commonUtils.common.CommonUtils
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.dialog.DialogUtil
import com.app.easyleasy.commonUtils.utility.extension.compressImageFile
import com.app.easyleasy.commonUtils.utility.extension.focusOnField
import com.app.easyleasy.commonUtils.utility.extension.getTrimText
import com.app.easyleasy.commonUtils.utility.extension.showSnackBar
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivitySendFeedbackBinding
import com.app.easyleasy.databinding.CvListItemFeedbackImageBinding
import com.app.easyleasy.dataclasses.FeedbackImageModel
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.viewModel.FeedbackViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.simpleadapter.SimpleAdapter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SendFeedbackActivity : BaseActivity<FeedbackViewModel>() {
    private var captureUri: Uri? = null
    private val MULTI_IMAGE_REQUEST_CODE = 303
    private lateinit var adapter: SimpleAdapter<FeedbackImageModel>
    private lateinit var binding: ActivitySendFeedbackBinding

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_send_feedback)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-sendFeedbackScreen","view_sendFeedBackScreen","view_sendFeedBackScreen")
        binding.apply {
            binding.inputBrief.setOnTouchListener { _, _ ->
                //Called when a touch event is dispatched to a view. This allows listeners to get a chance to respond before the target view.
                binding.scrollView.requestDisallowInterceptTouchEvent(true)
                false
            }

            binding.tvFeedback.text =
                String.format(getString(R.string.msg_report_problem), getString(R.string.app_name))

            btnBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK,"Back Button Click")
                finish()
            }
            btnSend.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK,"Send Button Click")
                MSCGenerator.addAction(GenConstants.ENTITY_USER, GenConstants.ENTITY_APP, "Send Feedback")
                performSend()
            }
        }

        addObservers()
        initRecycleView()
    }

    private fun addObservers() {
        viewModel.feedbackLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it.settings?.success == "1") {
                MSCGenerator.addAction(GenConstants.ENTITY_APP, GenConstants.ENTITY_USER, "Feedback sent")
                showMessage(it.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                Handler(mainLooper).postDelayed({ finish() }, 3000L)
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this)
            } else {
                MSCGenerator.addAction(GenConstants.ENTITY_APP, GenConstants.ENTITY_USER, "Feedback sent failed")
                showMessage(it.settings!!.message)
            }
        })
        viewModel.validationObserver.observe(this, Observer {
            binding.root.focusOnField(it.failedViewId)
            when(FEEDBACK_EMPTY){
                it.failType->{
                    getString(R.string.alert_enter_brief).showSnackBar(this)
                }
            }
        })
    }

    /**
     * Initialize recycle view
     */
    private fun initRecycleView() {
        adapter =
            SimpleAdapter.with<FeedbackImageModel, CvListItemFeedbackImageBinding>(R.layout.cv_list_item_feedback_image) { _, model, binding ->
                binding.model = model
            }
        binding.rvFeedbackImages.adapter = adapter

        adapter.addAll(viewModel.imageList)

        adapter.setClickableViews({ view, _, position ->
            if (view.id == R.id.ibtnAddImage) {
                checkPermission()
                //clickAddImage()
            } else if (view.id == R.id.ibtnRemoveImage) {
                removeSelectedImage(position)
            }
        }, R.id.ibtnAddImage, R.id.ibtnRemoveImage)
    }

    /**
     * Remove selected images from image list
     * @param position position of selected image
     */
    private fun removeSelectedImage(position: Int) {
        adapter.clear()
        adapter.addAll(viewModel.getImagesAfterRemove(position))
        adapter.notifyDataSetChanged()
    }

    private fun performSend() {
        if (checkInternet() && viewModel.isValid(binding.tietBriefFeedback.getTrimText())) {
            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
            viewModel.callReportProblem(binding.tietBriefFeedback.getTrimText())
        }
    }

    /**
     * Show option for capture image from camera or pick from gallery
     */
    private fun clickAddImage() {
        DialogUtil.confirmDialog(context = this@SendFeedbackActivity,
            title = "",
            msg = getString(R.string.msg_capture_image),
            positiveBtnText = getString(R.string.camera),
            negativeBtnText = getString(R.string.gallery),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    checkPermission()
                }

                override fun onCancel(isNeutral: Boolean) {
                    checkPermission()
                }
            })
    }

    fun checkPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.isAnyPermissionPermanentlyDenied) {
                        if (report.areAllPermissionsGranted()) {
                            ImagePicker.with(this@SendFeedbackActivity)
                                .crop() //Crop image(Optional), Check Customization for more option
                                .compress(1024) //Final image size will be less than 1 MB(Optional)
                                .maxResultSize(
                                    1080,
                                    1080
                                ) //Final image resolution will be less than 1080 x 1080(Optional)
                                .galleryMimeTypes( //Exclude gif images
                                    mimeTypes = arrayOf(
                                        "image/png",
                                        "image/jpg",
                                        "image/jpeg"
                                    )
                                ).start(MULTI_IMAGE_REQUEST_CODE)
                        }
                    } else {
                        showMessage(
                            getString(R.string.permission_denied_by_user)
                        )
                        CommonUtils.openApplicationSettings(this@SendFeedbackActivity)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                MULTI_IMAGE_REQUEST_CODE -> {
                    val fileUri = data?.data
                    captureUri = fileUri
                    handleImageRequest()
                }
            }

        }
    }

    /**
     * Show user's selected image in image list
     * @param image Selected image
     */
    private fun showSelectedImage(image: FeedbackImageModel) {
        adapter.clear()
        adapter.addAll(viewModel.getSelectedImage(image))
        adapter.notifyDataSetChanged()
    }


    private fun handleImageRequest() {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
        }

        GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
            val currentUri = captureUri
            Log.e("FILE - ", currentUri.toString())
            val imageFile = compressImageFile(currentUri!!)
            if (imageFile != null) {
                Log.e("FILE - ", imageFile.absolutePath)
                val newUri = FileProvider.getUriForFile(
                    this@SendFeedbackActivity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile
                )
                Log.e("FILE - ", newUri.toString())
                captureUri = newUri
                showSelectedImage(
                    FeedbackImageModel(
                        contentUri = captureUri,
                        imagePath = imageFile.absolutePath
                    )
                )
            }

            hideProgressDialog()
        }
    }
}