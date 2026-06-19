package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.model.AuthToken
import com.task.ibiproductsapp.domain.model.LoginCredentials
import com.task.ibiproductsapp.presentation.login.LoginViewModel
import com.task.ibiproductsapp.util.AppBiometricManager
import com.task.ibiproductsapp.util.DataStoreHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var dataStoreHelper: DataStoreHelper
    private lateinit var appBiometricManager: AppBiometricManager
    private lateinit var viewModel: LoginViewModel

    private val sampleToken = AuthToken(
        token = "abc123",
        refreshToken = "refresh123",
        userId = 1,
        username = "emilys",
        email = "emily@x.com",
        firstName = "Emily",
        lastName = "Johnson",
        image = ""
    )

    @Before
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)

        loginUseCase = mockk()
        dataStoreHelper = mockk()
        appBiometricManager = mockk()

        every { appBiometricManager.isBiometricAvailable() } returns false
        every { dataStoreHelper.isBiometricEnabled() } returns flowOf(false)
        every { dataStoreHelper.isLoggedIn() } returns flowOf(false)
        coEvery { dataStoreHelper.saveAuthToken(any(), any()) } returns Unit
        coEvery { dataStoreHelper.saveLoginSession(any()) } returns Unit

        viewModel = LoginViewModel(
            loginUseCase = loginUseCase,
            dataStoreHelper = dataStoreHelper,
            appBiometricManager = appBiometricManager,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `login success updates state to logged in and persists session`() =
        runTest(testDispatcher) {
            coEvery {
                loginUseCase(LoginCredentials(username = "emilys", password = "emilyspass"))
            } returns NetworkResult.Success(sampleToken)

            viewModel.onUsernameChanged("emilys")
            viewModel.onPasswordChanged("emilyspass")
            viewModel.login()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.loginState.value
            assertTrue(state.isLoggedIn)
            assertFalse(state.isLoading)
            coVerify(exactly = 1) { dataStoreHelper.saveAuthToken("abc123", "refresh123") }
            coVerify(exactly = 1) { dataStoreHelper.saveLoginSession("emilys") }
        }

    @Test
    fun `login failure sets error message and does not log in`() = runTest(testDispatcher) {
        coEvery {
            loginUseCase(LoginCredentials(username = "emilys", password = "wrongpass"))
        } returns NetworkResult.Error("Invalid credentials")

        viewModel.onUsernameChanged("emilys")
        viewModel.onPasswordChanged("wrongpass")
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.loginState.value
        assertFalse(state.isLoggedIn)
        assertEquals("Invalid credentials", state.errorMessage)
        coVerify(exactly = 0) { dataStoreHelper.saveAuthToken(any(), any()) }
    }

    @Test
    fun `onUsernameChanged clears existing error message`() {
        viewModel.onUsernameChanged("test")

        assertEquals(null, viewModel.loginState.value.errorMessage)
    }

    @Test
    fun `togglePasswordVisibility flips showPassword flag`() {
        val initial = viewModel.loginState.value.showPassword

        viewModel.togglePasswordVisibility()

        assertEquals(!initial, viewModel.loginState.value.showPassword)
    }
}