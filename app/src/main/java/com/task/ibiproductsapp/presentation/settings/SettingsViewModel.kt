package com.task.ibiproductsapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.ibiproductsapp.di.IoDispatcher
import com.task.ibiproductsapp.util.AppBiometricManager
import com.task.ibiproductsapp.util.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreHelper: DataStoreHelper,
    private val appBiometricManager: AppBiometricManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                dataStoreHelper.isDarkMode(),
                dataStoreHelper.getLanguage(),
                dataStoreHelper.isBiometricEnabled()
            ) { darkMode, language, biometric ->
                Triple(darkMode, language, biometric)
            }.collect { (darkMode, language, biometric) ->
                _settingsState.update {
                    it.copy(
                        isDarkMode = darkMode,
                        language = language,
                        isBiometricEnabled = biometric,
                        isBiometricAvailable = appBiometricManager.isBiometricAvailable()
                    )
                }
            }
        }
    }

    fun toggleDarkMode() {
        val current = _settingsState.value.isDarkMode
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dataStoreHelper.setDarkMode(!current)
            }
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dataStoreHelper.setLanguage(languageCode)
            }
        }
    }

    fun toggleBiometric() {
        val current = _settingsState.value.isBiometricEnabled
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dataStoreHelper.setBiometricEnabled(!current)
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        _settingsState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dataStoreHelper.clearSession()
            }
            onLoggedOut()
        }
    }
}