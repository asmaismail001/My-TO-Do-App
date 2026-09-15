package com.example.mytodoapp.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mytodoapp.MainActivity
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.repository.AppDatabase
import com.example.mytodoapp.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val action = intent.action
        val taskId = resolveTaskId(intent)

        android.util.Log.d(
            "ReminderReceiver",
            "onReceive action=$action taskId=$taskId extras=${intent.extras?.keySet()} data=${intent.data}"
        )

        if (action == ACTION_COMPLETE) {
            android.util.Log.d("ReminderReceiver", "ACTION_COMPLETE for taskId=$taskId")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId)

            val dao = AppDatabase.getDatabase(context).todoDao()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val todo = dao.getTodoById(taskId)
                    if (todo != null) {
                        if (todo.recurrence == com.example.mytodoapp.model.RecurrenceType.DAILY) {
                            val updated = todo.copy(lastCompletedDateMillis = System.currentTimeMillis(), completed = true)
                            dao.updateTodo(updated)
                            NotificationScheduler.scheduleNextOccurrence(context, updated)
                        } else {
                            dao.updateTodo(todo.copy(completed = true))
                            NotificationScheduler.cancelReminder(context, taskId)
                        }
                        val refreshIntent = Intent("com.example.mytodoapp.REFRESH_TODOS").apply {
                            `package` = context.packageName
                        }
                        context.sendBroadcast(refreshIntent)
                        android.util.Log.d("ReminderReceiver", "Task $taskId marked completed")
                    } else {
                        android.util.Log.w("ReminderReceiver", "Task $taskId not found for complete action")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ReminderReceiver", "Failed to mark task completed", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task"
        val dueTimeMillis = intent.getLongExtra("dueTimeMillis", 0L)
        val endTimeMillis = intent.getLongExtra("endTimeMillis", 0L)

        val dao = AppDatabase.getDatabase(context).todoDao()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val todo = if (taskId > 0) dao.getTodoById(taskId) else null
                android.util.Log.d(
                    "ReminderReceiver",
                    "DB lookup taskId=$taskId found=${todo != null} completed=${todo?.completed == true}"
                )
                when {
                    todo != null && todo.isCompletedForToday() -> {
                        android.util.Log.d("ReminderReceiver", "Task $taskId already completed for today. Skipping.")
                        if (todo.recurrence != com.example.mytodoapp.model.RecurrenceType.NONE) {
                            NotificationScheduler.scheduleNextOccurrence(context, todo)
                        }
                    }
                    todo != null -> {
                        val start = todo.dueTimeMillis ?: dueTimeMillis
                        val end = todo.endTimeMillis ?: endTimeMillis
                        showNotification(context, todo, todo.title, start, end)
                        if (todo.recurrence != com.example.mytodoapp.model.RecurrenceType.NONE) {
                            NotificationScheduler.scheduleNextOccurrence(context, todo)
                        }
                    }
                    taskId > 0 -> {
                        android.util.Log.w(
                            "ReminderReceiver",
                            "Task $taskId missing from DB; posting from intent extras anyway"
                        )
                        val fallback = Todo(
                            id = taskId,
                            title = taskTitle,
                            completed = false,
                            dueTimeMillis = dueTimeMillis.takeIf { it > 0 },
                            endTimeMillis = endTimeMillis.takeIf { it > 0 }
                        )
                        showNotification(context, fallback, taskTitle, dueTimeMillis, endTimeMillis)
                    }
                    else -> {
                        android.util.Log.e("ReminderReceiver", "No taskId on intent; cannot post notification")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderReceiver", "Error processing reminder", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun resolveTaskId(intent: Intent): Int {
        val extraId = intent.getIntExtra("taskId", 0)
        if (extraId > 0) return extraId
        return intent.data?.lastPathSegment?.toIntOrNull() ?: 0
    }

    private fun showNotification(
        context: Context,
        todo: Todo,
        taskTitle: String,
        dueTimeMillis: Long,
        endTimeMillis: Long
    ) {
        android.util.Log.d(
            "ReminderReceiver",
            "showNotification taskId=${todo.id} title=$taskTitle"
        )

        if (!PreferencesManager(context).areNotificationsEnabled()) {
            android.util.Log.w("ReminderReceiver", "In-app notifications toggle is OFF. Skipping post.")
            return
        }

        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                android.util.Log.e("ReminderReceiver", "POST_NOTIFICATIONS is not granted. Cannot post.")
                return
            }
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !manager.areNotificationsEnabled()) {
            android.util.Log.e("ReminderReceiver", "App notifications are disabled in system settings.")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = manager.getNotificationChannel(CHANNEL_ID)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                android.util.Log.e("ReminderReceiver", "Channel $CHANNEL_ID is blocked by the user.")
                return
            }
        }

        val timeSlotStr = formatTimeRange(dueTimeMillis, endTimeMillis)

        val openAppIntent = MainActivity.taskDetailsIntent(context, todo.id)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            todo.id,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, SnoozeActivity::class.java).apply {
            putExtra("taskId", todo.id)
            putExtra("taskTitle", taskTitle)
            putExtra("dueTimeMillis", dueTimeMillis)
            putExtra("endTimeMillis", endTimeMillis)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val snoozePendingIntent = PendingIntent.getActivity(
            context,
            todo.id + 100000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_COMPLETE
            data = UriFor(todo.id)
            setPackage(context.packageName)
            putExtra("taskId", todo.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            todo.id + 200000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.mytodoapp.R.drawable.ic_notification)
            .setContentTitle(todo.title)
            .setContentText(timeSlotStr.ifBlank { "Task reminder" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(timeSlotStr.ifBlank { "Task reminder" }))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(com.example.mytodoapp.R.drawable.ic_notification, "Snooze", snoozePendingIntent)
            .addAction(com.example.mytodoapp.R.drawable.ic_notification, "Complete", completePendingIntent)
            .build()

        try {
            android.util.Log.d("ReminderReceiver", "Posting notification id=${todo.id} on channel=$CHANNEL_ID")
            manager.notify(todo.id, notification)
            android.util.Log.d("ReminderReceiver", "Notification posted successfully for taskId=${todo.id}")
        } catch (e: SecurityException) {
            android.util.Log.e("ReminderReceiver", "SecurityException posting notification", e)
        } catch (e: Exception) {
            android.util.Log.e("ReminderReceiver", "Failed to post notification", e)
        }
    }

    private fun formatTimeRange(startMillis: Long, endMillis: Long): String {
        if (startMillis <= 0) return ""
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val startStr = timeFormat.format(Date(startMillis))
        val endStr = if (endMillis > 0) timeFormat.format(Date(endMillis)) else ""
        val dateStr = dateFormat.format(Date(startMillis))
        return if (endStr.isNotEmpty()) {
            "$dateStr, $startStr – $endStr"
        } else {
            "$dateStr, $startStr"
        }
    }

    companion object {
        // New id so a previously-created low-importance channel cannot mute reminders.
        const val CHANNEL_ID = "task_reminder_channel_v2"
        const val ACTION_COMPLETE = "com.example.mytodoapp.ACTION_COMPLETE"
        const val ACTION_REMINDER = "com.example.mytodoapp.ACTION_REMINDER"

        private fun UriFor(taskId: Int) = android.net.Uri.parse("todo://reminder/$taskId")

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you about scheduled tasks"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(sound, audioAttributes)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
