package com.app.easyleasy.view.authentication.signup

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.app.easyleasy.utility.validation.*
import com.app.easyleasy.BuildConfig
import com.app.easyleasy.R
import com.app.easyleasy.application.AppineersApplication
import com.app.easyleasy.application.AppineersApplication.Companion.sharedPreference
import com.app.easyleasy.commonUtils.common.*
import com.app.easyleasy.commonUtils.common.CommonUtils.Companion.openApplicationSettings
import com.app.easyleasy.commonUtils.common.isValidEmail
import com.app.easyleasy.commonUtils.common.isValidMobileNumber
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.PasswordStrength
import com.app.easyleasy.commonUtils.utility.extension.*
import com.app.easyleasy.commonUtils.utility.getDeviceName
import com.app.easyleasy.commonUtils.utility.getDeviceOSVersion
import com.app.easyleasy.dagger.components.ActivityComponent
import com.app.easyleasy.databinding.ActivitySignUpBinding
import com.app.easyleasy.dataclasses.Social
import com.app.easyleasy.dataclasses.response.StaticPage
import com.app.easyleasy.mvvm.BaseActivity
import com.app.easyleasy.view.authentication.otp.otpsignup.OTPSignUpActivity
import com.app.easyleasy.view.settings.SettingsFragment
import com.app.easyleasy.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.easyleasy.viewModel.SignUpViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
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
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*

class SignUpActivity : BaseActivity<SignUpViewModel>() {

    companion object {
        /**
         * Start intent to open signup activity with social information
         * @param mContext Context
         * @param social Social User's social information
         * @return Intent
         */
        fun getStartIntent(mContext: Context, social: Social): Intent {
            return Intent(mContext, SignUpActivity::class.java).apply {
                putExtra("social", social)
            }
        }

        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }

    private var imageUrl: String? = null
    private var binding: ActivitySignUpBinding? = null
    private var social: Social? = null
    private var captureUri: Uri? = null
    private var selectedPlace: Place? = null
    var placeSearchBy = IConstants.ADDRESS_SEARCH

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-signUpScreen", "view_signUpScreen", "view_signUpScreen")
        social = intent.getParcelableExtra("social")
        binding?.let {
            with(it,
                {
                    tietMobileNumber.addTextChangedListener(
                        PhoneNumberFormattingTextWatcher("US")
                    )

                    tvTermsAndPolicy.text = String.format(
                        getString(R.string.agree_terms_and_condition),
                        getString(R.string.app_name)
                    )
                }
            )
        }
        initListeners()
        setSocialInformation()

        binding?.tvTermsAndPolicy?.let {
            setBoldAndColorSpannable(
                it,
                getString(R.string.terms_n_conditions),
                getString(R.string.privacy_policy),
                getString(R.string.eula_policy)
            )
        }
    }

    /**
     * Set Social information, if user signup with social
     */
    private fun setSocialInformation() {
        if (social != null) {
            //captureUri = Uri.parse(social?.profileImageUrl.toString())
            if (social?.firstName?.isNotEmpty() == true) {
                binding?.tietFirstName?.setText(social?.firstName)
            }

            if (social?.lastName?.isNotEmpty() == true) {
                binding?.tietLastName?.setText(social?.lastName)
            }
            if (social?.emailId?.isNotEmpty() == true) {
                binding?.tietEmail?.setText(social?.emailId)
                binding?.tietEmail?.isEnabled = false
            }
            if (social?.name?.isNotEmpty() == true && social?.name?.contains(" ") == false) {
                binding?.tietUserName?.setText(social?.name)
                binding?.tietUserName?.isEnabled = false
            }

            binding?.tilConfirmPassword?.visibility = View.GONE
            binding?.tilPassword?.visibility = View.GONE

            if (social?.profileImageUrl?.isNotEmpty() == true) {
                // binding?.sivUserImage?.setImageResource(R.drawable.user_profile)
                downloadSocialImage(social?.profileImageUrl!!)
            }
        }
    }

    /**
     * Download user's social media profile picture to local for sign up
     * @param url String
     */
    private fun downloadSocialImage(url: String) {
        Glide.with(this@SignUpActivity)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageUrl = saveBitmapImage(
                        image = resource,
                        imageFileName = "JPEG_" + social?.name + ".jpg",
                        context = applicationContext
                    )
                    captureUri = Uri.parse(imageUrl)
                    logger.dumpCustomEvent(
                        "Social Image saved at ",
                        social?.profileImageUrl.toString()
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
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
                                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsFragment.STATIC_PAGE_TERMS_CONDITION,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SignUpActivity,
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
                                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsFragment.STATIC_PAGE_PRIVACY_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SignUpActivity,
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
                                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsFragment.STATIC_PAGE_EULA_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SignUpActivity,
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
                            this@SignUpActivity,
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

    private fun initListeners() {
        binding?.let {
            with(it, {

                ibtnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }

                tietDOB.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Select DOB Click")
                    setDOB()
                }
                tietAddress.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Select Address Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "choose location"
                    )
                    placeSearchBy = IConstants.ADDRESS_SEARCH
                    openPlacePicker()
                }
                tietCity.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Select City Click")
                    placeSearchBy = IConstants.CITY_SEARCH
                    openPlacePicker()
                }
                btnCreateAccount.setOnClickListener {
                    hideKeyboard()
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Sign up Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "sign up"
                    )
                    signUp()
                }

                btnAdd.setOnClickListener {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "add image"
                    )
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Profile Picture Click")
                    Dexter.withContext(this@SignUpActivity)
                        .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                        ).withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                if (!report.isAnyPermissionPermanentlyDenied) {
                                    if (report.areAllPermissionsGranted()) {
                                        ImagePicker.with(this@SignUpActivity)
                                            .crop()                    //Crop image(Optional), Check Customization for more option
                                            .compress(1024)            //Final image size will be less than 1 MB(Optional)
                                            .maxResultSize(
                                                1080,
                                                1080
                                            )    //Final image resolution will be less than 1080 x 1080(Optional)
                                            .galleryMimeTypes(  //Exclude gif images
                                                mimeTypes = arrayOf(
                                                    "image/png",
                                                    "image/jpg",
                                                    "image/jpeg"
                                                )
                                            )
                                            .start()
                                    }
                                } else {
                                    showMessage(
                                        getString(R.string.permission_denied_by_user)
                                    )
                                    openApplicationSettings(this@SignUpActivity)
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

            })
        }
    }

    /**
     * Open date picker and set date of birth
     */
    private fun setDOB() {
        val calendar = Calendar.getInstance()
        if (!binding?.tietDOB?.getTrimText().isNullOrEmpty()) {
            calendar.time = binding?.tietDOB?.getTrimText()?.toMMDDYYYDate() ?: Date()
        }
        val datePicker = DatePickerDialog(
            this@SignUpActivity,
            R.style.DatePickerTheme,
            { _, year, month, dayOfMonth ->
                binding?.tietDOB?.setText(getDateFromPicker(year, month, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePicker.show()
    }

    /**
     * Convert year, month and day into readable date
     * @param year Int
     * @param month Int
     * @param dayOfMonth Int
     * @return String
     */
    private fun getDateFromPicker(year: Int, month: Int, dayOfMonth: Int): String {
        val calender = Calendar.getInstance()
        calender.set(Calendar.YEAR, year)
        calender.set(Calendar.MONTH, month)
        calender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return calender.time.toMMDDYYYStr()
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
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    /**
     * Set address in input fields after select address from places api
     * @param place Place
     */
    private fun setAddress(place: Place) {
        selectedPlace = place
        val locationAddress =
            place.addressComponents?.let { super.getParseAddressComponents(addressComponents = it) }
        binding?.let {
            with(it,
                {
                    tietAddress.setText(locationAddress?.address)
                    tietCity.setText(locationAddress?.city)
                    tietZipCode.setText(locationAddress?.zipCode)
                    tietState.setText(locationAddress?.state)
                })
        }
        logger.debugEvent("Place Result", place.address ?: "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                AUTOCOMPLETE_REQUEST_CODE -> {
                    if (data != null) {
                        setAddress(Autocomplete.getPlaceFromIntent(data))
                        data.dataString?.let { Timber.d(it) }
                    }
                }

                else -> {
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
                    this@SignUpActivity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile
                )
                captureUri = newUri
                binding?.sivUserImage?.loadCircleImage(
                    imageFile.absolutePath,
                    R.drawable.user_profile
                )
                imageUrl = imageFile.absolutePath

            }

            hideProgressDialog()
        }
    }


    private fun signUp() {
        viewModel.signUpRequestModel.profileImage = getProfileImageUrl()
        viewModel.signUpRequestModel.firstName = binding?.tietFirstName?.getTrimText().toString()
        viewModel.signUpRequestModel.lastName = binding?.tietLastName?.getTrimText().toString()
        viewModel.signUpRequestModel.userName = binding?.tietUserName?.getTrimText().toString()
        viewModel.signUpRequestModel.email = binding?.tietEmail?.getTrimText().toString()
        viewModel.signUpRequestModel.mobileNumber =
            PhoneNumberUtils.normalizeNumber(binding?.tietMobileNumber?.getTrimText().toString())
        viewModel.signUpRequestModel.address = binding?.tietAddress?.getTrimText().toString()
        viewModel.signUpRequestModel.latitude = (selectedPlace?.latLng?.latitude ?: 0.0).toString()
        viewModel.signUpRequestModel.longitude =
            (selectedPlace?.latLng?.longitude ?: 0.0).toString()
        viewModel.signUpRequestModel.city = binding?.tietCity?.getTrimText().toString()
        viewModel.signUpRequestModel.state = binding?.tietState?.getTrimText().toString()
        viewModel.signUpRequestModel.zipCode = binding?.tietZipCode?.getTrimText().toString()
        viewModel.signUpRequestModel.dob =
            binding?.tietDOB?.getTrimText().toServerDateFormatString()
        viewModel.signUpRequestModel.password = binding?.tietPassword?.getTrimText().toString()
        viewModel.signUpRequestModel.confirmPassword =
            binding?.tietConfirmPassword?.getTrimText().toString()
        viewModel.signUpRequestModel.socialType = social?.type ?: ""
        viewModel.signUpRequestModel.socialId = social?.socialId ?: ""
        viewModel.signUpRequestModel.tnc = binding?.cbTermsAndPolicy?.isChecked!!
        viewModel.signUpRequestModel.deviceType = IConstants.DEVICE_TYPE_ANDROID
        viewModel.signUpRequestModel.deviceModel = getDeviceName()
        viewModel.signUpRequestModel.deviceOs = getDeviceOSVersion()
        viewModel.signUpRequestModel.deviceToken = sharedPreference.deviceToken ?: ""
        if (viewModel.isValid(viewModel.signUpRequestModel)) {
            hideKeyboard()
            showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
            if (checkInternet() && ((application as AppineersApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_EMAIL) ||
                ((application as AppineersApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_EMAIL_SOCIAL)
            ) {
                if (viewModel.signUpRequestModel.socialType.isEmpty()) {
                    viewModel.callSignUpWithEmail()
                } else {
                    viewModel.callSignUpWithSocial()
                }

            } else if (checkInternet() && ((application as AppineersApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_PHONE) ||
                ((application as AppineersApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_PHONE_SOCIAL)
            ) {
                viewModel.callCheckUnique(
                    type = "phone",
                    email = viewModel.signUpRequestModel.email,
                    phone = viewModel.signUpRequestModel.mobileNumber,
                    userName = viewModel.signUpRequestModel.userName
                )
            }
        }
    }

    private fun isValidSignUpRequest(): Boolean {
        when {
            !viewModel.signUpRequestModel.firstName.isValidText() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_first_name)
                    )
                }
                return false
            }

            !isOnlyAlphabateAndSpace(viewModel.signUpRequestModel.firstName) -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_first_name_character)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.firstName.isValidTextLength() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_first_name_length)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.lastName.isValidText() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_last_name_character)
                    )
                }
                return false
            }

            !isOnlyAlphabateAndSpace(viewModel.signUpRequestModel.lastName) -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_first_name)
                    )
                }
                return false
            }


            !viewModel.signUpRequestModel.lastName.isValidTextLength() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_last_name_length)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.email.isValidEmail() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_valid_email)
                    )
                }
                return false
            }
            !viewModel.signUpRequestModel.mobileNumber.isValidMobileNumber() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_phone_number)
                    )
                }
                return false
            }
            !viewModel.signUpRequestModel.mobileNumber.isValidMobileNumberLength() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_phone_number_length)
                    )
                }
                return false
            }

            !isOnlyAlphanumric(viewModel.signUpRequestModel.userName) -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_user_name_character)
                    )
                }
                return false
            }


            (placeSearchBy == IConstants.ADDRESS_SEARCH) && !viewModel.signUpRequestModel.address.isValidText() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_select_street_address)
                    )
                }
                return false
            }
            !viewModel.signUpRequestModel.city.isValidText() -> {
                runOnUiThread { showMessage(getString(R.string.alert_enter_city)) }
                return false
            }
            !viewModel.signUpRequestModel.state.isValidText() -> {
                runOnUiThread { showMessage(getString(R.string.alert_select_state)) }
                return false
            }
            (placeSearchBy == IConstants.ADDRESS_SEARCH) && !viewModel.signUpRequestModel.zipCode.isValidText() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_zip_code)
                    )
                }
                return false
            }

            (placeSearchBy == IConstants.ADDRESS_SEARCH) && !viewModel.signUpRequestModel.zipCode.isValidZipCodeLength() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_min_zip_code_length)
                    )
                }
                return false
            }

            !isOnlyAlphanumric(viewModel.signUpRequestModel.zipCode) -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_zip_code_character)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.dob.isValidText() -> {
                runOnUiThread { showMessage(getString(R.string.alert_enter_dob)) }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty() && viewModel.signUpRequestModel.password.isEmpty() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_password)
                    )
                }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty()
                    && viewModel.signUpRequestModel.confirmPassword.isEmpty()-> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_confirm_password)
                    )
                }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty() && viewModel.signUpRequestModel.confirmPassword.isValidPassword() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_valid_password)
                    )
                }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty()
                    && PasswordStrength.calculateStrength(viewModel.signUpRequestModel.password).value < PasswordStrength.STRONG.value -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_valid_password)
                    )
                }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty() && viewModel.signUpRequestModel.confirmPassword != viewModel.signUpRequestModel.password -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_confirm_password_not_match)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.tnc -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_accept_tnc_and_privacy)
                    )
                }
                return false
            }

            else -> return true
        }
    }

    private fun getProfileImageUrl(): String {
        return if (captureUri == null) {
            ""
        } else {
            imageUrl ?: ""
        }
    }


    override fun setupObservers() {
        super.setupObservers()
        viewModel.signUpLiveData.observe(this, { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up success"
                )
                showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                Handler(mainLooper).postDelayed(
                    { finish() },
                    3000L
                )
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up failed"
                )
                showMessage(response.settings!!.message)
                Timber.d(response.settings?.message)
            }
        })


        viewModel.signUpLiveDataSocial.observe(this, androidx.lifecycle.Observer {
            hideProgressDialog()
            if (it.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up success"
                )
                showMessage(it.settings!!.message)
                if (it.data == null) {
                    finish()
                } else {
                    viewModel.saveUserDetails(it.data!![0])
                    navigateToHomeScreen()
                }
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up failed"
                )
                showMessage(it.settings!!.message)
                Timber.d(it.settings?.message)
            }
        })

        viewModel.otpLiveData.observe(this, androidx.lifecycle.Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up success, phone verification"
                )
                showMessage(response.settings!!.message)
                navigateToPhoneVerificationScreen(response.data?.get(0)?.otp ?: "")
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up failed"
                )
                showMessage(response.settings!!.message)
                Timber.d(response.settings?.message)
            }
        })

        viewModel.validationObserver.observe(this, {
            binding?.root?.focusOnField(it.failedViewId)
            if (it.failType != null) {
                focusInvalidInput(it.failType!!)
            }
        })

    }

    /**
     * Navigate to [com.app.easyleasy.view.authentication.otp.otpsignup.OTPSignUpActivity] for verify phone number
     * @param otp String otp which is send to user's phone number
     */
    private fun navigateToPhoneVerificationScreen(otp: String) {
        startActivity(
            binding?.tietMobileNumber?.getTrimText()?.let {
                OTPSignUpActivity.getStartIntent(
                    context = this@SignUpActivity,
                    phoneNumber = it,
                    otp = otp, request = viewModel.signUpRequestModel
                )
            })
    }

    override fun onPause() {
        super.onPause()
        //internetConnection = false
    }

    /**
     * Show focus on invalid input field
     * @param failType Int
     */
    private fun focusInvalidInput(failType: Int) {
        when (failType) {
            EMAIL_EMPTY -> {
                showMessage(getString(R.string.alert_enter_email))
            }
            EMAIL_INVALID -> {
                showMessage(getString(R.string.alert_enter_valid_email))
            }
            EMAIL_LENGTH -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_max_email_length),
                        resources.getInteger(R.integer.email_max_length)
                    )
                )
            }
            USER_NAME_EMPTY -> {
                showMessage(getString(R.string.alert_enter_user_name))
            }
            USER_NAME_INVALID -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_min_user_name_length),
                        resources.getInteger(R.integer.user_name_min_length)
                    )
                )
            }

            USER_NAME_CHARACTER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_user_name_character))
            }

            FIRST_NAME_EMPTY -> {
                showMessage(getString(R.string.alert_enter_first_name))
            }
            FIRST_NAME_INVALID -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_min_first_name_length),
                        resources.getInteger(R.integer.first_name_min_length)
                    )
                )
            }

            FIRST_NAME_CHARACTER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_first_name_character))
            }

            LAST_NAME_EMPTY -> {
                showMessage(getString(R.string.alert_enter_last_name))
            }
            LAST_NAME_INVALID -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_min_last_name_length),
                        resources.getInteger(R.integer.first_name_min_length)
                    )
                )
            }

            LAST_NAME_CHARACTER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_last_name_character))
            }

            PHONE_NUMBER_EMPTY -> {
                showMessage(getString(R.string.alert_enter_mobile_number))
            }
            PHONE_NUMBER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_phone_number_format))
            }

            PHONE_NUMBER_INVALID_LENGHT -> {
                showMessage(getString(R.string.alert_invalid_phone_number))
            }

            DOB_EMPTY -> {
                showMessage(getString(R.string.alert_enter_dob))
            }
            ADDRESS_EMPTY -> {
                showMessage(getString(R.string.alert_select_street_address))
            }
            STATE_EMPTY -> {
                showMessage(getString(R.string.alert_select_state))
            }
            CITY_EMPTY -> {
                showMessage(getString(R.string.alert_enter_city))
            }
            ZIP_CODE_EMPTY -> {
                showMessage(getString(R.string.alert_enter_zip_code))
            }
            ZIP_CODE_INVALID -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_min_zip_code_length),
                        resources.getInteger(R.integer.zip_code_min_length)
                    )
                )
            }

            ZIP_CODE_CHARACTER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_zip_code_character))
            }

            PASSWORD_EMPTY -> {
                showMessage(getString(R.string.alert_enter_password))
            }
            PASSWORD_INVALID -> {
                showMessage(getString(R.string.alert_valid_password))
            }
            CONFORM_PASSWORD_EMPTY -> {
                showMessage(getString(R.string.alert_enter_confirm_password))
            }
            PASSWORD_NOT_MATCH -> {
                showMessage(getString(R.string.alert_msg_password_and_confirm_password_not_same))
            }
            TNC_NOT_ACCEPTED -> {
                showMessage(getString(R.string.alert_accept_tnc_and_privacy))
            }
        }
    }
}