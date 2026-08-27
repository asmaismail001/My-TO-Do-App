package com.example.mytodoapp.util

object ReminderTime {

    /**
     * Reminder alarm time = (due date + due time) minus [minutesBefore].
     * Returns null when that instant is not in the future so nothing is
     * posted at save time.
     */
    fun alarmTriggerMillis(
        dueDateTimeMillis: Long,
        minutesBefore: Int,
        nowMillis: Long
    ): Long? {
        val trigger = dueDateTimeMillis - minutesBefore.toLong().coerceAtLeast(0) * 60_000L
        return trigger.takeIf { it > nowMillis }
    }
}
