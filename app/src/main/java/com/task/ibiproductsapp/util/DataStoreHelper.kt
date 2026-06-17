package com.task.ibiproductsapp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ibi_prefs")

class DataStoreHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USERNAME = stringPreferencesKey("username")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val IS_DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
    }

    // Auth token — used synchronously by AuthInterceptor
    fun getAuthToken(): String = runBlocking {
        context.dataStore.data.first()[Keys.AUTH_TOKEN] ?: ""
    }

    suspend fun saveAuthToken(token: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTH_TOKEN] = token
            prefs[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveLoginSession(username: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USERNAME] = username
            prefs[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTH_TOKEN] = ""
            prefs[Keys.REFRESH_TOKEN] = ""
            prefs[Keys.USERNAME] = ""
            prefs[Keys.IS_LOGGED_IN] = false
        }
    }

    fun isLoggedIn(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: false
    }

    fun getUsername(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USERNAME] ?: ""
    }

    // Biometric
    fun isBiometricEnabled(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    // Theme
    fun isDarkMode(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_MODE] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = enabled
        }
    }

    // Language: "en" or "he"
    fun getLanguage(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "en"
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = languageCode
        }
    }
}
