package com.example.mytodoapp.util

import android.content.Context

class PreferencesManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getThemeMode(): String = prefs.getString("theme_mode", "system") ?: "system"
    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun isDarkTheme(): Boolean {
        return when (getThemeMode()) {
            "light" -> false
            "dark" -> true
            else -> {
                val uiMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

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

    fun getSavedLatitude(): Double = prefs.getString("weather_lat", "51.5074")?.toDoubleOrNull() ?: 51.5074
    fun getSavedLongitude(): Double = prefs.getString("weather_lon", "-0.1278")?.toDoubleOrNull() ?: -0.1278
    fun getSavedLocationName(): String = prefs.getString("weather_loc_name", "Current Location") ?: "Current Location"
    fun isManualLocation(): Boolean = prefs.getBoolean("weather_is_manual", false)

    fun saveWeatherLocation(lat: Double, lon: Double, name: String, isManual: Boolean) {
        prefs.edit()
            .putString("weather_lat", lat.toString())
            .putString("weather_lon", lon.toString())
            .putString("weather_loc_name", name)
            .putBoolean("weather_is_manual", isManual)
            .apply()
    }
}