package com.app.easyleasy.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.easyleasy.commonUtils.common.Resource
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.dataclasses.response.StaticPageResponse
import com.app.easyleasy.mainactivitytest.errorMsg
import com.app.easyleasy.mainactivitytest.getErrorResponse
import com.app.easyleasy.objectclasses.KotlinBaseMockObjectsClass
import com.app.easyleasy.repository.StaticPagesRepository
import com.app.easyleasy.utils.mock
import com.app.easyleasy.utils.whenever
import com.google.gson.JsonElement
import io.reactivex.Single
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class StaticPagesViewModelTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockStaticPagesRepositoryTest = mock<StaticPagesRepository>()
    private val mockUpdateTNCObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val mockStaticPageObserver = mock<Observer<TAListResponse<StaticPageResponse>>>()
    private val showDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    private val updateTNCResponse = TAListResponse<JsonElement>()
    private val emptyResponseListForTNC : ArrayList<JsonElement> = arrayListOf()

    private val staticPageResponse = TAListResponse<StaticPageResponse>()
    private val emptyResponseListForStaticPage : ArrayList<StaticPageResponse> = arrayListOf()

    private val viewModel by lazy {
        StaticPagesViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockStaticPagesRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockStaticPagesRepositoryTest,
            showDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyUpdateTNCError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callUpdateTNCPrivacyPolicy("type"))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockStaticPagesRepositoryTest.updateTNCPrivacyPolicy(ArgumentMatchers.anyString()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(0)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(0)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyUpdateTNCSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callUpdateTNCPrivacyPolicy("type"))
            .thenReturn(Single.just(TAListResponse<JsonElement>()))
        whenever(mockStaticPagesRepositoryTest.updateTNCPrivacyPolicy(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(getUpdateTNCSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.updateTNCResponseLiveData.observeForever(mockUpdateTNCObserver)
        viewModel.callUpdateTNCPrivacyPolicy("type")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockUpdateTNCObserver, Mockito.times(1)).onChanged(
                updateTNCResponse
            )
        }
    }

    private fun getUpdateTNCSuccessResponse() : TAListResponse<JsonElement>{
        updateTNCResponse.data = emptyResponseListForTNC
        return updateTNCResponse
    }

    @Test
    fun verifyStaticPagesError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callGetStaticPageData("code"))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockStaticPagesRepositoryTest.getStaticPageData(ArgumentMatchers.anyString()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(0)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(0)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyStaticPagesSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callGetStaticPageData("code"))
            .thenReturn(Single.just(TAListResponse<StaticPageResponse>()))
        whenever(mockStaticPagesRepositoryTest.getStaticPageData(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(getStaticPagesSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.staticPageResponseLiveData.observeForever(mockStaticPageObserver)
        viewModel.callStaticPage("code")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockStaticPageObserver, Mockito.times(1)).onChanged(
                staticPageResponse
            )
        }
    }

    private fun getStaticPagesSuccessResponse() : TAListResponse<StaticPageResponse>{
        staticPageResponse.data = emptyResponseListForStaticPage
        return staticPageResponse
    }
}