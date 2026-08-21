package com.example.mytodoapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object NotificationScheduler {

    private const val REMINDER_OFFSET_MILLIS = 60 * 60 * 1000L

    fun scheduleReminder(context: Context, taskId: Int, taskTitle: String, dueTimeMillis: Long) {
        val currentTime = System.currentTimeMillis()
        if (dueTimeMillis <= currentTime) return

        var reminderTime = dueTimeMillis - REMINDER_OFFSET_MILLIS
        if (reminderTime <= currentTime) {
            reminderTime = currentTime + 1000L
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("taskTitle", taskTitle)
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Exact alarm permission not granted
        }
    }

    fun cancelReminder(context: Context, taskId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}