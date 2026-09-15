package com.example.mytodoapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mytodoapp.repository.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Android cancels all AlarmManager alarms on device reboot (and some OEMs clear
 * them on app updates too). Without this receiver, every previously-scheduled
 * task reminder silently disappears after a restart and never fires again.
 * This receiver re-reads all incomplete, notification-enabled, future tasks
 * from the DB and re-schedules their reminders.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(appContext).todoDao()
                val todos = dao.getAllTodos()
                todos.forEach { todo ->
                    val shouldSchedule = todo.notificationEnabled &&
                        (todo.endTimeMillis != null || todo.dueTimeMillis != null) &&
                        (!todo.completed || todo.recurrence != com.example.mytodoapp.model.RecurrenceType.NONE)

                    if (shouldSchedule) {
                        NotificationScheduler.scheduleReminder(
                            appContext,
                            todo.id,
                            todo.title,
                            todo.dueTimeMillis ?: 0L,
                            todo.endTimeMillis,
                            todo.notificationMinutesBefore,
                            todo.recurrence
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}