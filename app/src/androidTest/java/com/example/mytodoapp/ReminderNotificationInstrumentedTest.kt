package com.example.mytodoapp

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.notification.NotificationScheduler
import com.example.mytodoapp.notification.ReminderReceiver
import com.example.mytodoapp.repository.TodoRepository
import com.example.mytodoapp.util.ReminderTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderNotificationInstrumentedTest {

    @Test
    fun creatingTaskDoesNotPostNotificationImmediately() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        uiAutomation.executeShellCommand(
            "appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow"
        ).close()

        ReminderReceiver.ensureChannel(context)

        val now = System.currentTimeMillis()
        val dueTime = now + 2 * 60 * 60 * 1000L
        val expectedTrigger = ReminderTime.alarmTriggerMillis(dueTime, 10, now)
        assertEquals(dueTime - 10 * 60_000L, expectedTrigger)

        val saved = runBlocking {
            TodoRepository(context).addTodo(
                title = "No immediate notify",
                description = "",
                priority = Priority.HIGH,
                dueTimeMillis = now + 60 * 60 * 1000L,
                endTimeMillis = dueTime,
                notificationEnabled = true,
                notificationMinutesBefore = 10
            )
        }

        NotificationScheduler.scheduleReminder(
            context,
            saved.id,
            saved.title,
            now + 60 * 60 * 1000L,
            dueTime,
            10
        )

        Thread.sleep(3_000L)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val posted = manager.activeNotifications.any { it.id == saved.id }
        assertFalse(
            "Creating a task must not post a notification immediately. active=${manager.activeNotifications.map { it.id }}",
            posted
        )
    }

    @Test
    fun reminderAlarmPostsNotificationAtScheduledDueOffset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        uiAutomation.executeShellCommand(
            "appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow"
        ).close()

        ReminderReceiver.ensureChannel(context)

        val dueTime = System.currentTimeMillis() + 6_000L
        val saved = runBlocking {
            TodoRepository(context).addTodo(
                title = "Instrumented reminder test",
                description = "",
                priority = Priority.HIGH,
                dueTimeMillis = dueTime - 1_000L,
                endTimeMillis = dueTime,
                notificationEnabled = true,
                notificationMinutesBefore = 0
            )
        }

        NotificationScheduler.scheduleReminder(
            context,
            saved.id,
            saved.title,
            dueTime - 1_000L,
            dueTime,
            0
        )

        Thread.sleep(2_000L)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertFalse(
            "Notification must wait until reminder time. active=${manager.activeNotifications.map { it.id }}",
            manager.activeNotifications.any { it.id == saved.id }
        )

        Thread.sleep(10_000L)
        val posted = manager.activeNotifications.any { it.id == saved.id }
        assertTrue(
            "Expected a posted notification for task ${saved.id}. active=${manager.activeNotifications.map { it.id }}",
            posted
        )
    }
}
