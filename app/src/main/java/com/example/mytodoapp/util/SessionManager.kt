package com.example.mytodoapp.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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

    fun logout() {
        prefs.edit().clear().apply()
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
    }
}
