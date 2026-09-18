package com.example.mytodoapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.mytodoapp.MainActivity
import com.example.mytodoapp.model.RecurrenceType
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.util.ReminderTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationScheduler {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun calculateNextTriggerMillis(
        dueTimeMillis: Long,
        endTimeMillis: Long?,
        minutesBefore: Int,
        recurrence: RecurrenceType,
        currentTime: Long = System.currentTimeMillis()
    ): Long? {
        val dueDateTime = if (endTimeMillis != null && endTimeMillis > 0L) {
            endTimeMillis
        } else {
            dueTimeMillis
        }

        if (recurrence == RecurrenceType.NONE) {
            return ReminderTime.alarmTriggerMillis(dueDateTime, minutesBefore, currentTime)
        }

        if (recurrence == RecurrenceType.DAILY) {
            val baseCal = Calendar.getInstance().apply { timeInMillis = dueDateTime }
            val targetHour = baseCal.get(Calendar.HOUR_OF_DAY)
            val targetMinute = baseCal.get(Calendar.MINUTE)

            val candidate = Calendar.getInstance().apply {
                timeInMillis = currentTime
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, -minutesBefore)
            }

            if (candidate.timeInMillis <= currentTime) {
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
            return candidate.timeInMillis
        }

        if (recurrence == RecurrenceType.WEEKLY) {
            val baseCal = Calendar.getInstance().apply { timeInMillis = dueDateTime }
            val targetDayOfWeek = baseCal.get(Calendar.DAY_OF_WEEK)
            val targetHour = baseCal.get(Calendar.HOUR_OF_DAY)
            val targetMinute = baseCal.get(Calendar.MINUTE)

            val candidate = Calendar.getInstance().apply {
                timeInMillis = currentTime
                set(Calendar.DAY_OF_WEEK, targetDayOfWeek)
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, -minutesBefore)
            }

            if (candidate.timeInMillis <= currentTime) {
                candidate.add(Calendar.WEEK_OF_YEAR, 1)
            }
            return candidate.timeInMillis
        }

        if (recurrence == RecurrenceType.MONTHLY) {
            val baseCal = Calendar.getInstance().apply { timeInMillis = dueDateTime }
            val targetDayOfMonth = baseCal.get(Calendar.DAY_OF_MONTH)
            val targetHour = baseCal.get(Calendar.HOUR_OF_DAY)
            val targetMinute = baseCal.get(Calendar.MINUTE)

            val candidate = Calendar.getInstance().apply {
                timeInMillis = currentTime
                set(Calendar.DAY_OF_MONTH, targetDayOfMonth)
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, -minutesBefore)
            }

            if (candidate.timeInMillis <= currentTime) {
                candidate.add(Calendar.MONTH, 1)
            }
            return candidate.timeInMillis
        }

        return ReminderTime.alarmTriggerMillis(dueDateTime, minutesBefore, currentTime)
    }

    fun scheduleReminder(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long,
        endTimeMillis: Long?,
        minutesBefore: Int,
        recurrence: RecurrenceType = RecurrenceType.NONE
    ) {
        val appContext = context.applicationContext
        val currentTime = System.currentTimeMillis()
        val triggerTime = calculateNextTriggerMillis(
            dueTimeMillis = dueTimeMillis,
            endTimeMillis = endTimeMillis,
            minutesBefore = minutesBefore,
            recurrence = recurrence,
            currentTime = currentTime
        )

        if (triggerTime == null) {
            android.util.Log.w(
                "NotificationScheduler",
                "scheduleReminder skipped: reminder is not in the future. " +
                    "taskId=$taskId recurrence=$recurrence minutesBefore=$minutesBefore now=${fmt(currentTime)}"
            )
            return
        }

        android.util.Log.d(
            "NotificationScheduler",
            "scheduleReminder taskId=$taskId title=$taskTitle recurrence=$recurrence " +
                "minutesBefore=$minutesBefore trigger=${fmt(triggerTime)} now=${fmt(currentTime)}"
        )

        val pendingIntent = reminderPendingIntent(
            appContext,
            taskId,
            taskTitle,
            dueTimeMillis,
            endTimeMillis ?: 0L,
            recurrence
        )
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleAlarm(appContext, alarmManager, triggerTime, pendingIntent, taskId)
    }

    fun scheduleNextOccurrence(context: Context, todo: Todo) {
        if (!todo.notificationEnabled) return
        val due = todo.dueTimeMillis ?: return
        scheduleReminder(
            context = context,
            taskId = todo.id,
            taskTitle = todo.title,
            dueTimeMillis = due,
            endTimeMillis = todo.endTimeMillis,
            minutesBefore = todo.notificationMinutesBefore,
            recurrence = todo.recurrence
        )
    }

    fun scheduleSnooze(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long?,
        endTimeMillis: Long?,
        snoozeMinutes: Int,
        recurrence: RecurrenceType = RecurrenceType.NONE
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
            endTimeMillis ?: 0L,
            recurrence
        )
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleAlarm(appContext, alarmManager, reminderTime, pendingIntent, taskId)
    }

    fun cancelReminder(context: Context, taskId: Int) {
        android.util.Log.d("NotificationScheduler", "cancelReminder taskId=$taskId")
        val appContext = context.applicationContext
        val pendingIntent = reminderPendingIntent(appContext, taskId, "", 0L, 0L, RecurrenceType.NONE)
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    internal fun reminderIntent(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueTimeMillis: Long,
        endTimeMillis: Long,
        recurrence: RecurrenceType = RecurrenceType.NONE
    ): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            data = reminderUri(taskId)
            setPackage(context.packageName)
            putExtra("taskTitle", taskTitle)
            putExtra("taskId", taskId)
            putExtra("dueTimeMillis", dueTimeMillis)
            putExtra("endTimeMillis", endTimeMillis)
            putExtra("recurrence", recurrence.name)
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
        endTimeMillis: Long,
        recurrence: RecurrenceType = RecurrenceType.NONE
    ): PendingIntent {
        val intent = reminderIntent(context, taskId, taskTitle, dueTimeMillis, endTimeMillis, recurrence)
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
