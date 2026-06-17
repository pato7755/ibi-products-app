package com.task.ibiproductsapp.presentation.settings


data class SettingsState(
    val isDarkMode: Boolean = false,
    val language: String = "en",
    val isBiometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isLoggingOut: Boolean = false
)