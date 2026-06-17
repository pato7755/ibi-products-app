package com.task.ibiproductsapp.presentation.login

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.ibiproductsapp.common.BiometricResult
import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.di.IoDispatcher
import com.task.ibiproductsapp.domain.model.LoginCredentials
import com.task.ibiproductsapp.domain.usecase.LoginUseCase
import com.task.ibiproductsapp.util.AppBiometricManager
import com.task.ibiproductsapp.util.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val dataStoreHelper: DataStoreHelper,
    private val appBiometricManager: AppBiometricManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    init {
        checkBiometricAvailability()
        checkExistingSession()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val isAvailable = appBiometricManager.isBiometricAvailable()
            val isEnabled = dataStoreHelper.isBiometricEnabled().first()
            _loginState.update {
                it.copy(
                    isBiometricAvailable = isAvailable,
                    isBiometricEnabled = isEnabled
                )
            }
        }
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val isLoggedIn = dataStoreHelper.isLoggedIn().first()
            if (isLoggedIn) {
                _loginState.update { it.copy(isLoggedIn = true) }
            }
        }
    }

    fun onUsernameChanged(value: String) {
        _loginState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _loginState.update { it.copy(password = value, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _loginState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun login() {
        val state = _loginState.value
        _loginState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                loginUseCase(
                    LoginCredentials(
                        username = state.username.trim(),
                        password = state.password
                    )
                )
            }

            when (result) {
                is NetworkResult.Success -> {
                    dataStoreHelper.saveAuthToken(result.data.token, result.data.refreshToken)
                    dataStoreHelper.saveLoginSession(result.data.username)
                    _loginState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is NetworkResult.Error -> {
                    _loginState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun loginWithBiometric(activity: FragmentActivity) {
        appBiometricManager.authenticate(
            activity = activity,
            onResult = { result ->
                when (result) {
                    is BiometricResult.Success -> {
                        viewModelScope.launch {
                            // Persist the session so the app remembers on next open
                            dataStoreHelper.saveLoginSession(
                                dataStoreHelper.getUsername().first()
                            )
                            _loginState.update { it.copy(isLoggedIn = true) }
                        }
                    }
                    is BiometricResult.Error -> {
                        _loginState.update { it.copy(errorMessage = result.message) }
                    }
                    is BiometricResult.Cancelled -> { /* User dismissed — do nothing */ }
                    else -> {
                        _loginState.update {
                            it.copy(errorMessage = "Biometric authentication not available")
                        }
                    }
                }
            }
        )
    }

    fun clearError() {
        _loginState.update { it.copy(errorMessage = null) }
    }
}