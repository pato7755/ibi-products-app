package com.task.ibiproductsapp.presentation.login

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val showPassword: Boolean = false
)