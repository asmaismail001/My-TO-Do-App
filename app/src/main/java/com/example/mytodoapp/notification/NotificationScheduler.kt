package com.example.mytodoapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object NotificationScheduler {

    private const val REMINDER_OFFSET_MILLIS = 60 * 60 * 1000L

    fun scheduleReminder(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long,
        endTimeMillis: Long?,
        minutesBefore: Int
    ) {
        val currentTime = System.currentTimeMillis()
        if (dueTimeMillis <= currentTime) return

        var reminderTime = dueTimeMillis - (minutesBefore * 60 * 1000L)
        if (reminderTime <= currentTime) {
            reminderTime = currentTime + 1000L
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            setPackage(context.packageName)
            putExtra("taskTitle", taskTitle)
            putExtra("taskId", taskId)
            putExtra("dueTimeMillis", dueTimeMillis)
            putExtra("endTimeMillis", endTimeMillis ?: 0L)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Exact alarm permission not granted, fallback to non-exact alarm
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun scheduleSnooze(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long?,
        endTimeMillis: Long?,
        snoozeMinutes: Int
    ) {
        val currentTime = System.currentTimeMillis()
        val reminderTime = currentTime + snoozeMinutes * 60 * 1000L

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            setPackage(context.packageName)
            putExtra("taskTitle", taskTitle)
            putExtra("taskId", taskId)
            putExtra("dueTimeMillis", dueTimeMillis ?: 0L)
            putExtra("endTimeMillis", endTimeMillis ?: 0L)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, taskId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            setPackage(context.packageName)
        }
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