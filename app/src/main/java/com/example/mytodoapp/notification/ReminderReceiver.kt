package com.example.mytodoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.repository.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val action = intent.action
        val taskId = intent.getIntExtra("taskId", 0)

        if (action == ACTION_COMPLETE) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId)

            val dao = AppDatabase.getDatabase(context).todoDao()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val todos = dao.getAllTodos()
                    val todo = todos.find { it.id == taskId }
                    if (todo != null) {
                        dao.updateTodo(todo.copy(completed = true))
                        NotificationScheduler.cancelReminder(context, taskId)
                        // Trigger UI reload if app is open
                        val refreshIntent = Intent("com.example.mytodoapp.REFRESH_TODOS").apply {
                            `package` = context.packageName
                        }
                        context.sendBroadcast(refreshIntent)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task"
        val dueTimeMillis = intent.getLongExtra("dueTimeMillis", 0L)
        val endTimeMillis = intent.getLongExtra("endTimeMillis", 0L)

        // Query database to ensure the task still exists and is not complete
        val dao = AppDatabase.getDatabase(context).todoDao()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val todos = dao.getAllTodos()
                val todo = todos.find { it.id == taskId }
                if (todo != null && !todo.completed) {
                    withContext(Dispatchers.Main) {
                        showNotification(context, todo, taskTitle, dueTimeMillis, endTimeMillis)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        todo: Todo,
        taskTitle: String,
        dueTimeMillis: Long,
        endTimeMillis: Long
    ) {
        createNotificationChannel(context)

        val timeSlotStr = formatTimeRange(dueTimeMillis, endTimeMillis)

        // Intent to launch transparent Snooze dialog activity
        val snoozeIntent = Intent(context, SnoozeActivity::class.java).apply {
            putExtra("taskId", todo.id)
            putExtra("taskTitle", taskTitle)
            putExtra("dueTimeMillis", dueTimeMillis)
            putExtra("endTimeMillis", endTimeMillis)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val snoozePendingIntent = PendingIntent.getActivity(
            context,
            todo.id + 100000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to complete the task in background
        val completeIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = ACTION_COMPLETE
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
            .setContentText(timeSlotStr)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .addAction(com.example.mytodoapp.R.drawable.ic_notification, "Snooze", snoozePendingIntent)
            .addAction(com.example.mytodoapp.R.drawable.ic_notification, "Complete", completePendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(todo.id, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
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

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you about scheduled tasks"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "task_reminder_channel"
        const val ACTION_COMPLETE = "com.example.mytodoapp.ACTION_COMPLETE"
        const val ACTION_REMINDER = "com.example.mytodoapp.ACTION_REMINDER"
    }
}