package com.task.ibiproductsapp.util

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.task.ibiproductsapp.common.BiometricResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class AppBiometricManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isBiometricAvailable(): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getBiometricStatus(): BiometricResult {
        val manager = /*Android*/BiometricManager.from(context)
        return when (manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricResult.Success
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricResult.NotEnrolled
            else -> BiometricResult.NotAvailable
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Biometric Login",
        subtitle: String = "Use your fingerprint or face to login",
        negativeButtonText: String = "Use Password",
        onResult: (BiometricResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onResult(BiometricResult.Cancelled)
                    else -> onResult(BiometricResult.Error(errString.toString()))
                }
            }

            override fun onAuthenticationFailed() {
                // Fingerprint not recognised — BiometricPrompt handles retries automatically.
                // Only terminal failures call onAuthenticationError, so no action needed here.
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}

fun Context.findActivity(): FragmentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No FragmentActivity found")
}