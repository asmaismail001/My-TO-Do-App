package com.example.mytodoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mytodoapp.util.CalendarUtil

enum class Priority { HIGH, MEDIUM, LOW }

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val completed: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val dueTimeMillis: Long? = null,
    val endTimeMillis: Long? = null,
    val attachmentUri: String? = null,
    val notificationEnabled: Boolean = false,
    val notificationMinutesBefore: Int = 10,
    val userId: String? = null,
    val taskType: TaskType = TaskType.FLEXIBLE,
    val tags: List<String> = emptyList(),
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val lastCompletedDateMillis: Long? = null
) {
    /**
     * Checks if this task is completed for the current day.
     * For normal tasks, returns [completed].
     * For recurring tasks (e.g. DAILY), returns true if [lastCompletedDateMillis] matches today's date.
     */
    fun isCompletedForToday(): Boolean {
        if (recurrence == RecurrenceType.NONE) return completed
        val lastComp = lastCompletedDateMillis ?: return false
        return CalendarUtil.isSameDay(lastComp, System.currentTimeMillis())
    }

    /**
     * Checks if this task was/is completed for a given target calendar date.
     */
    fun isEffectiveCompleted(targetDateMillis: Long = System.currentTimeMillis()): Boolean {
        if (recurrence == RecurrenceType.NONE) return completed
        val lastComp = lastCompletedDateMillis ?: return false
        return CalendarUtil.isSameDay(lastComp, targetDateMillis)
    }
}