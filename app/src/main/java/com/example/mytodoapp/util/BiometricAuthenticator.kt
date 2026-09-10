package com.example.mytodoapp.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

sealed class BiometricStatus {
    object Available : BiometricStatus()
    object NotEnrolled : BiometricStatus()
    data class Unavailable(val reason: String) : BiometricStatus()
}

sealed class BiometricResult {
    object Success : BiometricResult()
    object Failed : BiometricResult()
    data class Cancelled(val errorCode: Int, val message: String) : BiometricResult()
    data class Error(val errorCode: Int, val message: String) : BiometricResult()
}

object BiometricAuthenticator {

    private const val AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun canAuthenticate(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.Unavailable("No biometric hardware available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.Unavailable("Biometric hardware is currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.Unavailable("Biometric authentication is unsupported on this device.")
            else -> BiometricStatus.Unavailable("Biometric authentication is not available.")
        }
    }

    fun isAvailable(context: Context): Boolean {
        return canAuthenticate(context) is BiometricStatus.Available
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "App Locked",
        subtitle: String = "Verify your fingerprint to continue using Task Manager",
        negativeButtonText: String = "Cancel",
        onResult: (BiometricResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(BiometricResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val message = errString.toString()
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onResult(BiometricResult.Cancelled(errorCode, message))
                    } else {
                        onResult(BiometricResult.Error(errorCode, message))
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(BiometricResult.Failed)
                }
            }
        )

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onResult(BiometricResult.Error(-1, e.localizedMessage ?: "Failed to initiate biometric prompt"))
        }
    }
}
