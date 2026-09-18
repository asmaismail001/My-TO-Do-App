package com.example.mytodoapp.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationPermissionHelper {

    /**
     * Checks if the app currently has permission to post notifications.
     * On Android 13+ (API 33+), checks both POST_NOTIFICATIONS runtime permission and system notification settings.
     * On Android < 13, checks system notification settings.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        val areSystemNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!areSystemNotificationsEnabled) {
            return false
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Determines whether we should show the "Open Settings" redirect instead of requesting permission.
     * Returns true if:
     * - System notifications are disabled in system settings for this app.
     * - On Android 13+, permission has been requested before and shouldShowRequestPermissionRationale is false (permanently denied).
     */
    fun shouldShowSettingsRedirect(activity: Activity?, prefs: PreferencesManager? = null): Boolean {
        if (activity == null) return false

        // If notifications are completely disabled in system settings:
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            return true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (isGranted) return false

            val hasRequestedBefore = prefs?.hasRequestedNotificationPermission() ?: false
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )

            // If requested before and shouldShowRationale is false, Android considers it permanently denied / "Don't ask again"
            return hasRequestedBefore && !shouldShowRationale
        }

        return false
    }

    /**
     * Unwraps a Context to find its parent Activity, if any.
     */
    fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Opens the app's notification settings directly without hardcoding package names.
     * Uses Settings.ACTION_APP_NOTIFICATION_SETTINGS on Android 8+ (API 26+)
     * and falls back to Settings.ACTION_APPLICATION_DETAILS_SETTINGS.
     */
    fun openNotificationSettings(context: Context) {
        val packageName = context.packageName
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {
                // Silently ignore if no settings intent can be resolved
            }
        }
    }
}
