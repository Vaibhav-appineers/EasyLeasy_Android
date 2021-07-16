package com.app.easyleasy.view.settings.editprofile

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.app.easyleasy.utility.validation.*
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.app.easyleasy.commonUtils.common.CommonUtils
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.dialog.ImageSourceDialog
import com.app.easyleasy.commonUtils.utility.extension.*
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivityEditProfileBinding
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.gallery.GalleryPagerActivity
import com.app.easyleasy.viewModel.UserProfileViewModel
import com.github.dhaval2404.imagepicker.BuildConfig
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EditProfileActivity : BaseActivity<UserProfileViewModel>() {

    private var captureUri: Uri? = null
    private var imagePath: String = ""
    private var selectedPlace: Place? = null
    var binding: ActivityEditProfileBinding? = null
    var placeSearchBy = IConstants.CITY_SEARCH
    private var currentProfilePathToDelete : String? = ""

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-editProfileScreen",
            "view_editProfileScreen",
            "view_editProfileScreen"
        )
        binding?.user = sharedPreference.userDetail
        binding?.tietMobileNumber?.addTextChangedListener(PhoneNumberFormattingTextWatcher("US"))
        binding?.apply {

            tietMobileNumber.addTextChangedListener(
                PhoneNumberFormattingTextWatcher("US")
            )

            ibtnBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()
            }

            tietAddress.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Select Address Click")
                placeSearchBy = IConstants.ADDRESS_SEARCH
                openPlacePicker()
            }

            tietCity.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Select City Click")
                placeSearchBy = IConstants.CITY_SEARCH
                openPlacePicker()
            }

            tietDOB.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Select DOB Click")
                setDOB()
            }
            btnUpdate.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Validate and Submit Click")
                MSCGenerator.addAction(
                    GenConstants.ENTITY_USER,
                    GenConstants.ENTITY_APP,
                    "Validate and Submit Profile"
                )
                //validate()
                performEditProfile()

            }

            btnAdd.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Image Button Click")
                checkPermission()
            }
            sivUserImage.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Image Button Click")
                startActivity(
                    GalleryPagerActivity.getStartIntent(
                        this@EditProfileActivity, arrayListOf(
                            com.app.easyleasy.commonUtils.utility.extension.sharedPreference.userDetail?.profileImage
                                ?: ""
                        ), 0
                    )
                )
            }
        }
    }

    /**
     * Perform edit profile
     */
    private fun performEditProfile() {
        val signUpRequest = viewModel.getEditProfileRequest(
            userProfileImage = imagePath,
            userName = binding?.tietUserName!!.getTrimText(),
            firstName = binding?.tietFirstName!!.getTrimText(),
            lastName = binding?.tietLastName!!.getTrimText(),
            phoneNumber = if (binding?.tietMobileNumber!!.getTrimText()
                    .isEmpty()
            ) "" else  PhoneNumberUtils.normalizeNumber(binding?.tietMobileNumber?.getTrimText().toString()),
            dob = binding?.tietDOB!!.getTrimText().toServerDateFormatString(),
            /*address = if (selectedPlace != null) selectedPlace?.address
                    ?: "" else binding.inputAddress.getTrimText(),*/
            address = binding?.tietAddress!!.getTrimText(),
            latitude = if (selectedPlace != null) (selectedPlace?.latLng?.latitude
                ?: 0.0).toString() else sharedPreference.userDetail?.latitude ?: "0.0",
            longitude = if (selectedPlace != null) (selectedPlace?.latLng?.longitude
                ?: 0.0).toString() else sharedPreference.userDetail?.longitude ?: "0.0",
            city = binding?.tietCity!!.getTrimText(),
            state = binding?.tietState!!.getTrimText(),
            zip = binding?.tietZipCode!!.getTrimText()
        )

        if (viewModel.isValid(signUpRequest)) {
            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
            viewModel.updateUserProfile(signUpRequest)
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        hideProgressDialog()
        viewModel.updateUserLiveData.observe(this, androidx.lifecycle.Observer {
            hideProgressDialog()
            if (it.settings?.success == "1") {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Profile updated"
                )
                showMessage(it.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                Handler(mainLooper).postDelayed(
                    {
                        sharedPreference.userDetail = it.data!![0]
                        (application as AppineersApplication).isProfileUpdated.value = true
                        finish()
                    }, IConstants.SNAKE_BAR_SHOW_TIME
                )
            } else if (!handleApiError(it.settings)) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Profile updation failed"
                )
                it?.settings?.message?.showSnackBar(this@EditProfileActivity)
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Profile updation failed"
                )
                showMessage(it.settings!!.message)
            } 
        })

        viewModel.validationObserver.observe(this@EditProfileActivity, androidx.lifecycle.Observer {
            binding?.root?.focusOnField(it.failedViewId)
            if (it.failType != null)
                focusInvalidInput(it.failType!!)
        })
    }

    private fun focusInvalidInput(failType: Int) {
        binding?.apply {
            when (failType) {
                FIRST_NAME_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_enter_first_name)
                        )
                    }
                    tietFirstName.requestFocus()
                }
                FIRST_NAME_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_first_name_character)
                        )
                    }
                    tietFirstName.requestFocus()
                }

                FIRST_NAME_CHARACTER_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_first_name_character)
                        )
                    }
                    tietFirstName.requestFocus()
                }

                LAST_NAME_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_enter_last_name)
                        )
                    }
                    tietLastName.requestFocus()
                }
                LAST_NAME_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_last_name_character)
                        )
                    }
                    tietLastName.requestFocus()
                }

                LAST_NAME_CHARACTER_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_last_name_character)
                        )
                    }
                    tietLastName.requestFocus()
                }

                USER_NAME_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_enter_user_name)
                        )
                    }
                    tietUserName.requestFocus()
                }
                USER_NAME_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_user_name_character)
                        )
                    }
                    tietUserName.requestFocus()
                }
                USER_NAME_CHARACTER_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_user_name_character)
                        )
                    }
                    tietUserName.requestFocus()
                }
                PHONE_NUMBER_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_phone_number)
                        )
                    }
                    tietMobileNumber.requestFocus()
                }
                PHONE_NUMBER_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_phone_number_format)
                        )
                    }
                    tietMobileNumber.requestFocus()
                }

                PHONE_NUMBER_INVALID_LENGHT -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_phone_number)
                        )
                    }
                    tietMobileNumber.requestFocus()
                }

                DOB_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_enter_dob)
                        )
                    }
                    tietDOB.requestFocus()
                }
                ADDRESS_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_select_street_address)
                        )
                    }
                    tietAddress.requestFocus()
                }
                STATE_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_select_state)
                        )
                    }
                    tietState.requestFocus()
                }
                CITY_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_enter_city)
                        )
                    }
                    tietCity.requestFocus()
                }
                ZIP_CODE_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_enter_zip_code)
                        )
                    }
                    tietZipCode.requestFocus()
                }
                ZIP_CODE_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_zip_code_character)
                        )
                    }
                    tietZipCode.requestFocus()
                }
                ZIP_CODE_CHARACTER_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_min_zip_code_length)
                        )
                    }
                    tietZipCode.requestFocus()
                }
            }
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }


    /**
     * Open date picker and set date of birth
     */
    private fun setDOB() {
        val calendar = Calendar.getInstance()
        if (binding?.tietDOB?.getTrimText()?.isNotEmpty()!!) {
            calendar.time = binding!!.tietDOB.getTrimText().toMMDDYYYDate()
        }
        val datePicker = DatePickerDialog(
            this@EditProfileActivity, R.style.DatePickerTheme,
            { _, year, month, dayOfMonth ->
                binding!!.tietDOB.setText(viewModel.getDateFromPicker(year, month, dayOfMonth))
                binding!!.tietDOB.error = null
            }, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePicker.show()
    }

    /**
     * Open auto complete place picker to get address
     */
    private fun openPlacePicker() {
        hideKeyboard()
        Places.initialize(applicationContext, "AIzaSyAyB7asyhW7JK6hyK90S_Ow_ai145KH14Y")
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields =
            listOf(Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        val intent: Intent
        if (placeSearchBy == IConstants.CITY_SEARCH) {
            intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
            ).setTypeFilter(TypeFilter.CITIES).setCountry("US")
                .build(this)
        } else {
            intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
            ).setCountry("US").build(this)
        }
        startActivityForResult(intent, IConstants.AUTOCOMPLETE_REQUEST_CODE)
    }


    private fun checkPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.isAnyPermissionPermanentlyDenied) {
                        if (report.areAllPermissionsGranted()) {
                            currentProfilePathToDelete = ""
                            if (report.areAllPermissionsGranted()) {
                                ImageSourceDialog(this@EditProfileActivity, onPhotoRemove = {
                                    binding!!.sivUserImage.setImageResource(R.drawable.user_profile)
                                    currentProfilePathToDelete = sharedPreference.userDetail?.profileImage
                                }).show(supportFragmentManager, "image source dialog")
                            }
                        }
                    } else {
                        showMessage(
                            getString(R.string.permission_denied_by_user)
                        )
                        CommonUtils.openApplicationSettings(this@EditProfileActivity)
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
                IConstants.AUTOCOMPLETE_REQUEST_CODE -> {
                    if (data != null) {
                        setAddress(Autocomplete.getPlaceFromIntent(data))
                    }
                }
                IConstants.MULTI_IMAGE_REQUEST_CODE -> {
                    val fileUri = data?.data
                    captureUri = fileUri
                    handleImageRequest()
                }

            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            if (data != null) {
                val status = Autocomplete.getStatusFromIntent(data)
                logger.debugEvent("Place Picker", status.statusMessage ?: "")
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            logger.debugEvent("Place Picker", "Result Canceled")
        }

    }


    private fun handleImageRequest() {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
        }

        GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
            val currentUri = captureUri
            val imageFile = compressImageFile(currentUri!!)
            if (imageFile != null) {
                val newUri = FileProvider.getUriForFile(
                    this@EditProfileActivity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile
                )
                captureUri = newUri
                binding?.sivUserImage?.loadCircleImage(
                    imageFile.absolutePath,
                    R.drawable.user_profile
                )
                imagePath = imageFile.absolutePath

            }

            hideProgressDialog()
        }
    }

    private fun setAddress(place: Place) {
        selectedPlace = place
        val locationAddress =
            place.addressComponents?.let { super.getParseAddressComponents(addressComponents = it) }
        binding?.apply {
            tietAddress.setText(locationAddress?.address)
            tietState.setText(locationAddress?.state)
            tietCity.setText(locationAddress?.city)
            tietZipCode.setText(locationAddress?.zipCode)
            tietAddress.error = null
            tietState.error = null
            tietCity.error = null
            tietZipCode.error = null
        }
        logger.debugEvent("Place Result", place.address ?: "")
    }

}