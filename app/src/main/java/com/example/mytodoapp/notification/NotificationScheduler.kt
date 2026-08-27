package com.example.mytodoapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.mytodoapp.MainActivity
import com.example.mytodoapp.util.ReminderTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationScheduler {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun scheduleReminder(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long,
        endTimeMillis: Long?,
        minutesBefore: Int
    ) {
        val appContext = context.applicationContext
        val currentTime = System.currentTimeMillis()
        val dueDateTime = if (endTimeMillis != null && endTimeMillis > 0L) {
            endTimeMillis
        } else {
            dueTimeMillis
        }
        val triggerTime = ReminderTime.alarmTriggerMillis(dueDateTime, minutesBefore, currentTime)

        if (triggerTime == null) {
            android.util.Log.w(
                "NotificationScheduler",
                "scheduleReminder skipped: reminder is not in the future. " +
                    "taskId=$taskId due=${fmt(dueDateTime)} minutesBefore=$minutesBefore now=${fmt(currentTime)}"
            )
            return
        }

        android.util.Log.d(
            "NotificationScheduler",
            "scheduleReminder taskId=$taskId title=$taskTitle " +
                "due=${fmt(dueDateTime)} minutesBefore=$minutesBefore " +
                "trigger=${fmt(triggerTime)} now=${fmt(currentTime)}"
        )

        val pendingIntent = reminderPendingIntent(
            appContext,
            taskId,
            taskTitle,
            dueTimeMillis,
            endTimeMillis ?: 0L
        )
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleAlarm(appContext, alarmManager, triggerTime, pendingIntent, taskId)
    }

    fun scheduleSnooze(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long?,
        endTimeMillis: Long?,
        snoozeMinutes: Int
    ) {
        val appContext = context.applicationContext
        val currentTime = System.currentTimeMillis()
        val reminderTime = currentTime + snoozeMinutes.toLong().coerceAtLeast(1) * 60_000L

        android.util.Log.d(
            "NotificationScheduler",
            "scheduleSnooze taskId=$taskId title=$taskTitle snoozeMinutes=$snoozeMinutes " +
                "trigger=${fmt(reminderTime)} now=${fmt(currentTime)}"
        )

        val pendingIntent = reminderPendingIntent(
            appContext,
            taskId,
            taskTitle,
            dueTimeMillis ?: 0L,
            endTimeMillis ?: 0L
        )
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleAlarm(appContext, alarmManager, reminderTime, pendingIntent, taskId)
    }

    fun cancelReminder(context: Context, taskId: Int) {
        android.util.Log.d("NotificationScheduler", "cancelReminder taskId=$taskId")
        val appContext = context.applicationContext
        val pendingIntent = reminderPendingIntent(appContext, taskId, "", 0L, 0L)
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    internal fun reminderIntent(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long,
        endTimeMillis: Long
    ): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            data = reminderUri(taskId)
            setPackage(context.packageName)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            putExtra("taskTitle", taskTitle)
            putExtra("taskId", taskId)
            putExtra("dueTimeMillis", dueTimeMillis)
            putExtra("endTimeMillis", endTimeMillis)
        }
    }

    private fun reminderUri(taskId: Int): Uri = Uri.parse("todo://reminder/$taskId")

    private fun pendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun reminderPendingIntent(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long,
        endTimeMillis: Long
    ): PendingIntent {
        val intent = reminderIntent(context, taskId, taskTitle, dueTimeMillis, endTimeMillis)
        return PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            pendingIntentFlags()
        )
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerTime: Long,
        pendingIntent: PendingIntent,
        taskId: Int
    ) {
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        android.util.Log.d(
            "NotificationScheduler",
            "scheduleAlarm taskId=$taskId trigger=${fmt(triggerTime)} canScheduleExact=$canScheduleExact"
        )

        try {
            if (canScheduleExact) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    taskId + 300000,
                    MainActivity.taskDetailsIntent(context, taskId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, showIntent),
                    pendingIntent
                )
                android.util.Log.d(
                    "NotificationScheduler",
                    "Alarm registered with setAlarmClock for taskId=$taskId at ${fmt(triggerTime)}"
                )
            } else {
                android.util.Log.w(
                    "NotificationScheduler",
                    "Exact alarm permission DENIED. Falling back to inexact setAndAllowWhileIdle."
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e(
                "NotificationScheduler",
                "SecurityException scheduling alarm for taskId=$taskId. Falling back to inexact.",
                e
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (inner: Exception) {
                android.util.Log.e("NotificationScheduler", "Failed to schedule any alarm", inner)
            }
        }
    }

    private fun fmt(millis: Long): String = timeFormat.format(Date(millis))
}
