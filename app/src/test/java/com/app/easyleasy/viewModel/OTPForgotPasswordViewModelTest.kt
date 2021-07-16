package com.app.easyleasy.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.forgotpasswordwithphone.ResetWithPhone
import com.app.easyleasy.mainactivitytest.errorMsg
import com.app.easyleasy.mainactivitytest.getErrorResponse
import com.app.easyleasy.objectclasses.KotlinBaseMockObjectsClass
import com.app.easyleasy.repository.OTPForgotPasswordRepository
import com.app.easyleasy.utils.mock
import com.app.easyleasy.utils.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class OTPForgotPasswordViewModelTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockOtpForgotPasswordRepositoryTest = mock<OTPForgotPasswordRepository>()
    private val resendOtpObserver = mock<Observer<TAListResponse<ResetWithPhone>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    //for resend otp
    private val resendOtpResponse = TAListResponse<ResetWithPhone>()
    private val emptyListResponse : ArrayList<ResetWithPhone> = arrayListOf()

    private val viewModel by lazy {
        OTPForgotPasswordViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockOtpForgotPasswordRepositoryTest,
            mockApplication
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockOtpForgotPasswordRepositoryTest,
            mockApplication,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyResendOtpError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callForgotPasswordWithPhone("9784595651"))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockOtpForgotPasswordRepositoryTest.getOTPForgotPasswordPhoneResponse(ArgumentMatchers.anyString()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(0)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(0)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyResendOtpSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callForgotPasswordWithPhone("9545954400"))
            .thenReturn(Single.just(TAListResponse<ResetWithPhone>()))
        whenever(mockOtpForgotPasswordRepositoryTest.getOTPForgotPasswordPhoneResponse(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(getResendOtpSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.resendOtpLiveData.observeForever(resendOtpObserver)
        viewModel.getOTPForgotPasswordWithPhone("9545954400")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(resendOtpObserver, Mockito.times(1)).onChanged(
                resendOtpResponse
            )
        }
    }

    private fun getResendOtpSuccessResponse() : TAListResponse<ResetWithPhone>?{
        resendOtpResponse.data = emptyListResponse
        return resendOtpResponse
    }
}