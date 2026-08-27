package com.example.mytodoapp

import com.example.mytodoapp.util.ReminderTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReminderTimeTest {

    private fun at(hour: Int, minute: Int): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.AUGUST, 26, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun fiveMinutesBeforeDueIs455pm() {
        val due = at(17, 0)
        val now = at(15, 0)
        val trigger = ReminderTime.alarmTriggerMillis(due, minutesBefore = 5, nowMillis = now)
        assertEquals(at(16, 55), trigger)
    }

    @Test
    fun tenMinutesBeforeDueIs450pm() {
        val due = at(17, 0)
        val now = at(15, 0)
        val trigger = ReminderTime.alarmTriggerMillis(due, minutesBefore = 10, nowMillis = now)
        assertEquals(at(16, 50), trigger)
    }

    @Test
    fun thirtyMinutesBeforeDueIs430pm() {
        val due = at(17, 0)
        val now = at(15, 0)
        val trigger = ReminderTime.alarmTriggerMillis(due, minutesBefore = 30, nowMillis = now)
        assertEquals(at(16, 30), trigger)
    }

    @Test
    fun oneHourBeforeDueIs400pm() {
        val due = at(17, 0)
        val now = at(15, 0)
        val trigger = ReminderTime.alarmTriggerMillis(due, minutesBefore = 60, nowMillis = now)
        assertEquals(at(16, 0), trigger)
    }

    @Test
    fun doesNotScheduleWhenReminderIsNotInTheFuture() {
        val due = at(17, 0)
        val now = at(16, 56)
        assertNull(ReminderTime.alarmTriggerMillis(due, minutesBefore = 5, nowMillis = now))
    }

    @Test
    fun doesNotUseCreationTimeAsTheTrigger() {
        val due = at(17, 0)
        val createdAt = at(15, 0)
        val trigger = ReminderTime.alarmTriggerMillis(due, minutesBefore = 10, nowMillis = createdAt)
        assertEquals(at(16, 50), trigger)
        assertEquals(false, trigger == createdAt)
    }
}
