package com.example.mytodoapp.util

import android.content.Context

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isDarkTheme(): Boolean = prefs.getBoolean("dark_theme", false)
    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("dark_theme", enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun getProfilePictureUri(): String? = prefs.getString("profile_picture_uri", null)
    fun setProfilePictureUri(uri: String?) {
        prefs.edit().putString("profile_picture_uri", uri).apply()
    }
}