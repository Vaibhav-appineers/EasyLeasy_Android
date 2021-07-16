package com.app.easyleasy.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.easyleasy.commonUtils.utility.IConstants
import com.app.easyleasy.commonUtils.utility.getDeviceName
import com.app.easyleasy.commonUtils.utility.getDeviceOSVersion
import com.app.easyleasy.dataclasses.generics.TAListResponse
import com.app.easyleasy.objectclasses.KotlinBaseMockObjectsClass
import com.app.easyleasy.utils.mock
import com.app.easyleasy.utils.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class LoginWithPhoneNumberRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockloginWithPhoneNumberRepositoryTest = mock<LoginWithPhoneNumberRepository>()
    private val loginWithPhoneNumberRepository by lazy {
        LoginWithPhoneNumberRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, loginWithPhoneNumberRepository.networkService)
    }

    @Test
    fun callLoginWithPhoneNumber(){
        val map = HashMap<String, String>()
        map["mobile_number"] = "8000154545"
        map["password"] = "Test@123"
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = "bzV6dFNLJjE2MTE1NTkxNTM="
        whenever(mockNetworkService.callLoginWithPhone(map = map))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockloginWithPhoneNumberRepositoryTest.callLoginWithPhoneNumber(map = map))
            .thenReturn(Single.just(TAListResponse()))
        loginWithPhoneNumberRepository.callLoginWithPhoneNumber(map = map)
            .test().assertComplete()
    }


}