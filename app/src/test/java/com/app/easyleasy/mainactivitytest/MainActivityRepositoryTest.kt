package com.app.easyleasy.mainactivitytest


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.easyleasy.objectclasses.KotlinBaseMockObjectsClass
import com.app.easyleasy.BuildConfig
import com.app.easyleasy.utils.mock
import com.app.easyleasy.utils.whenever
import com.app.easyleasy.repository.CentralRepository
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class MainActivityRepositoryTest : KotlinBaseMockObjectsClass() {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    val mockMainActivityRepositoryTest = mock<CentralRepository>()
    val centralRepository by lazy {
        CentralRepository(mockNetworkService, mockDataRepository)
    }

    @Before
    fun initTest() {
        Mockito.reset(mockNetworkService, mockDataRepository, mockApplication)
    }

    @Test
    fun verifyConstructorParameters() {
        assertEquals(mockNetworkService, centralRepository.networkService)
        assertEquals(mockDataRepository, centralRepository.WeatherDataRepository)
    }

    @Test
    fun verifyGetWeatherByCityName() {
        whenever(mockNetworkService.getWeatherByCity("Delhi", BuildConfig.openweathermapapikey))
            .thenReturn(Single.just(WeatherDataClass()))
        whenever(mockMainActivityRepositoryTest.getWeatherByCity(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(WeatherDataClass()))
        centralRepository.getWeatherByCity("Delhi").test().assertComplete()
    }
}