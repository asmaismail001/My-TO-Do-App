package com.example.mytodoapp.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val fileName = "secure_user_prefs"
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SessionManager", "Failed to initialize EncryptedSharedPreferences, attempting reset...", e)
            try {
                context.deleteSharedPreferences(fileName)
                val sharedPrefsDir = File(context.filesDir.parent, "shared_prefs")
                val prefsFile = File(sharedPrefsDir, "$fileName.xml")
                if (prefsFile.exists()) {
                    prefsFile.delete()
                }

                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    context,
                    fileName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (fallbackEx: Exception) {
                Log.e("SessionManager", "Falling back to standard SharedPreferences", fallbackEx)
                context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            }
        }
    }

    fun saveAuthToken(token: String?) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUserSession(userId: String, name: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun setUserPhone(phone: String?) {
        prefs.edit().putString(KEY_USER_PHONE, phone).apply()
    }

    fun getUserPhone(): String? = prefs.getString(KEY_USER_PHONE, null)

    fun isBiometricLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_LOCK_ENABLED, false)
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK_ENABLED, enabled).apply()
        if (!enabled) {
            clearLastBackgroundTime()
        }
    }

    fun saveLastBackgroundTime(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_BACKGROUND_TIME, timestamp).apply()
    }

    fun getLastBackgroundTime(): Long {
        return prefs.getLong(KEY_LAST_BACKGROUND_TIME, 0L)
    }

    fun clearLastBackgroundTime() {
        prefs.edit().remove(KEY_LAST_BACKGROUND_TIME).apply()
    }

    fun logout() {
        prefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_PHONE)
            remove(KEY_LAST_BACKGROUND_TIME)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_BIOMETRIC_LOCK_ENABLED = "biometric_lock_enabled"
        private const val KEY_LAST_BACKGROUND_TIME = "last_background_timestamp"
    }
}
