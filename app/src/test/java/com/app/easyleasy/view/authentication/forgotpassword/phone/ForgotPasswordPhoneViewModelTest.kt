package com.app.easyleasy.view.authentication.forgotpassword.phone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.easyleasy.objectclasses.KotlinBaseMockObjectsClass
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.forgotpasswordwithphone.ResetWithPhone
import com.app.easyleasy.mainactivitytest.errorMsg
import com.app.easyleasy.mainactivitytest.getErrorResponse
import com.app.easyleasy.repository.ForgotPasswordPhoneRepository
import com.app.easyleasy.utils.mock
import com.app.easyleasy.utils.whenever
import com.app.easyleasy.viewModel.ForgotPasswordPhoneViewModel
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class ForgotPasswordPhoneViewModelTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockForgotPasswordPhoneRepositoryTest = mock<ForgotPasswordPhoneRepository>()
    private val forgotPasswordObserver = mock<Observer<TAListResponse<ResetWithPhone>>>()
    private val showDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    private val serverBankAccountsReponse = TAListResponse<ResetWithPhone>()
    private val ezBankAccountList: ArrayList<ResetWithPhone> = arrayListOf()

    private val viewModel by lazy {
        ForgotPasswordPhoneViewModel(
                testSchedulerProvider,
                mockCompositeDisposable,
                mockNetworkHelper,
                mockForgotPasswordPhoneRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
                mockCompositeDisposable,
                mockNetworkHelper,
                mockForgotPasswordPhoneRepositoryTest,
                showDialogObserver,
                messageStringObserver
        )
    }

    @Test
    fun verifyForgotPasswordByPhoneError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callForgotPasswordWithPhone("9545954400"))
                .thenReturn(Single.error(getErrorResponse()))
        whenever(mockForgotPasswordPhoneRepositoryTest.getForgotPasswordPhoneResponse(ArgumentMatchers.anyString()))
                .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        viewModel.getForgotPasswordWithPhone("9036451252")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(1)).onChanged(
                    Resource.error(
                            errorMsg
                    )
            )
        }
    }

    @Test
    fun verifyForgotPasswordByPhoneSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callForgotPasswordWithPhone("9545954400"))
                .thenReturn(Single.just(TAListResponse<ResetWithPhone>()))
        whenever(mockForgotPasswordPhoneRepositoryTest.getForgotPasswordPhoneResponse(ArgumentMatchers.anyString()))
                .thenReturn(Single.just(getForgotPasswordByPhoneSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.forgotPasswordWithPhoneLiveData.observeForever(forgotPasswordObserver)
        viewModel.getForgotPasswordWithPhone("9545954400")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(forgotPasswordObserver, Mockito.times(1))
                    .onChanged(serverBankAccountsReponse)
        }
    }

    private fun getForgotPasswordByPhoneSuccessResponse(): TAListResponse<ResetWithPhone>? {
        serverBankAccountsReponse.data = ezBankAccountList
        return serverBankAccountsReponse
    }

}