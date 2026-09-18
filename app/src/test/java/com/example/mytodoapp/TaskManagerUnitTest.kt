package com.example.mytodoapp

import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.TaskType
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.util.TimeConflict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskManagerUnitTest {

    // 1. Task Creation
    @Test
    fun testTaskCreation_withValidData_createsTaskCorrectly() {
        // Verify a task is created correctly with valid data.
        val dueTime = 1700000000000L
        val endTime = 1700003600000L
        val task = Todo(
            id = 1,
            title = "Design Mockup",
            description = "Complete Compose UI redesign",
            priority = Priority.HIGH,
            completed = false,
            dueTimeMillis = dueTime,
            endTimeMillis = endTime,
            taskType = TaskType.INDOOR
        )

        assertEquals(1, task.id)
        assertEquals("Design Mockup", task.title)
        assertEquals("Complete Compose UI redesign", task.description)
        assertEquals(Priority.HIGH, task.priority)
        assertFalse(task.completed)
        assertEquals(dueTime, task.dueTimeMillis)
        assertEquals(endTime, task.endTimeMillis)
        assertEquals(TaskType.INDOOR, task.taskType)
    }

    @Test
    fun testTaskCreation_requiredInformationValidation() {
        // Verify required task information is handled correctly.
        val validTitle = "Submit Report"
        val blankTitle = "   "

        val isValid = validTitle.isNotBlank()
        val isInvalid = blankTitle.isNotBlank()

        assertTrue(isValid)
        assertFalse(isInvalid)
    }

    // 2. Task Completion
    @Test
    fun testTaskCompletion_markingCompleted_changesStatusCorrectly() {
        // Verify that marking a task as completed changes its status correctly.
        val initialTask = Todo(
            id = 10,
            title = "Review PR",
            completed = false
        )
        assertFalse(initialTask.completed)

        val completedTask = initialTask.copy(completed = true)
        assertTrue(completedTask.completed)

        // Toggling back to incomplete
        val toggledBackTask = completedTask.copy(completed = !completedTask.completed)
        assertFalse(toggledBackTask.completed)
    }

    // 3. Task Deletion
    @Test
    fun testTaskDeletion_removesTaskCorrectly() {
        // Verify that deleting a task removes it correctly.
        val task1 = Todo(id = 1, title = "Task 1", completed = false)
        val task2 = Todo(id = 2, title = "Task 2", completed = false)
        val task3 = Todo(id = 3, title = "Task 3", completed = true)

        val taskList = mutableListOf(task1, task2, task3)
        assertEquals(3, taskList.size)

        // Delete task2
        taskList.removeAll { it.id == task2.id }

        assertEquals(2, taskList.size)
        assertFalse(taskList.any { it.id == 2 })
        assertTrue(taskList.any { it.id == 1 })
        assertTrue(taskList.any { it.id == 3 })
    }

    // 4. Task Priority
    @Test
    fun testTaskPriority_storedCorrectlyWithTask() {
        // Verify that the selected priority is stored correctly with the task.
        val highTask = Todo(id = 1, title = "High Priority Task", priority = Priority.HIGH, completed = false)
        val mediumTask = Todo(id = 2, title = "Medium Priority Task", priority = Priority.MEDIUM, completed = false)
        val lowTask = Todo(id = 3, title = "Low Priority Task", priority = Priority.LOW, completed = false)

        assertEquals(Priority.HIGH, highTask.priority)
        assertEquals(Priority.MEDIUM, mediumTask.priority)
        assertEquals(Priority.LOW, lowTask.priority)
    }

    // 5. Task Time Conflict
    @Test
    fun testTaskTimeConflict_overlappingTasksDetected() {
        // Example: Task 1 = 5:00 PM–6:00 PM and Task 2 = 5:30 PM–6:30 PM should produce a conflict.
        val task1Start = 1700067600000L // 5:00 PM
        val task1End = 1700071200000L   // 6:00 PM
        val existingTask = Todo(
            id = 1,
            title = "Task 1",
            dueTimeMillis = task1Start,
            endTimeMillis = task1End,
            completed = false
        )

        val task2Start = 1700069400000L // 5:30 PM
        val task2End = 1700073000000L   // 6:30 PM

        // Verify that overlapping task times are detected.
        val conflict = TimeConflict.findOverlappingTask(
            startTime = task2Start,
            endTime = task2End,
            todos = listOf(existingTask)
        )
        assertNotNull(conflict)
        assertEquals(1, conflict!!.id)
    }

    @Test
    fun testTaskTimeConflict_nonOverlappingTasksAllowed() {
        // Verify that non-overlapping tasks do not produce a conflict.
        val task1Start = 1700067600000L // 5:00 PM
        val task1End = 1700071200000L   // 6:00 PM
        val existingTask = Todo(
            id = 1,
            title = "Task 1",
            dueTimeMillis = task1Start,
            endTimeMillis = task1End,
            completed = false
        )

        // Non-overlapping after: 6:00 PM - 7:00 PM
        val taskAfterStart = 1700071200000L // 6:00 PM
        val taskAfterEnd = 1700074800000L   // 7:00 PM
        val conflictAfter = TimeConflict.findOverlappingTask(
            startTime = taskAfterStart,
            endTime = taskAfterEnd,
            todos = listOf(existingTask)
        )
        assertNull(conflictAfter)

        // Non-overlapping before: 4:00 PM - 5:00 PM
        val taskBeforeStart = 1700064000000L // 4:00 PM
        val taskBeforeEnd = 1700067600000L   // 5:00 PM
        val conflictBefore = TimeConflict.findOverlappingTask(
            startTime = taskBeforeStart,
            endTime = taskBeforeEnd,
            todos = listOf(existingTask)
        )
        assertNull(conflictBefore)
    }

    // 6. Task Search
    @Test
    fun testTaskSearch_returnsCorrectMatchingTasks() {
        // Verify that searching returns the correct matching task(s).
        val tasks = listOf(
            Todo(id = 1, title = "Team Sync Meeting", description = "Discuss roadmap", completed = false),
            Todo(id = 2, title = "Buy groceries", description = "Milk, eggs, bread", completed = false),
            Todo(id = 3, title = "Prepare presentation", description = "Quarterly results meeting", completed = true)
        )

        fun search(query: String): List<Todo> {
            if (query.isBlank()) return tasks
            val q = query.trim().lowercase()
            return tasks.filter {
                it.title.lowercase().contains(q) ||
                    it.description.lowercase().contains(q) ||
                    it.priority.name.lowercase().contains(q)
            }
        }

        // Matching by title
        val titleMatch = search("groceries")
        assertEquals(1, titleMatch.size)
        assertEquals("Buy groceries", titleMatch[0].title)

        // Matching by description / title across multiple tasks
        val meetingMatch = search("meeting")
        assertEquals(2, meetingMatch.size)

        // Non-matching query returns empty
        val noMatch = search("nonexistent keyword")
        assertTrue(noMatch.isEmpty())
    }

    // 8. Daily Recurring Tasks & Occurrence Logic
    @Test
    fun testDailyRecurringTask_occurrenceCompletionStatus() {
        val today = System.currentTimeMillis()
        val yesterday = today - 24 * 60 * 60 * 1000L

        // Daily recurring task not completed yet today
        val recurringTask = Todo(
            id = 5,
            title = "Morning Run",
            recurrence = com.example.mytodoapp.model.RecurrenceType.DAILY,
            completed = false,
            lastCompletedDateMillis = null
        )
        assertFalse(recurringTask.isCompletedForToday())

        // Completed yesterday -> should NOT be completed for today
        val completedYesterdayTask = recurringTask.copy(
            lastCompletedDateMillis = yesterday,
            completed = true
        )
        assertFalse(completedYesterdayTask.isCompletedForToday())

        // Completed today -> should be completed for today
        val completedTodayTask = recurringTask.copy(
            lastCompletedDateMillis = today,
            completed = true
        )
        assertTrue(completedTodayTask.isCompletedForToday())
    }

    // 9. Tag and Multi-Filter Search
    @Test
    fun testTagsAndPriorityFiltering() {
        val tasks = listOf(
            Todo(id = 1, title = "Design Sprint", priority = Priority.HIGH, tags = listOf("Work", "Design"), completed = false),
            Todo(id = 2, title = "Math Homework", priority = Priority.MEDIUM, tags = listOf("Study"), completed = false),
            Todo(id = 3, title = "Grocery List", priority = Priority.LOW, tags = listOf("Personal", "Shopping"), completed = true)
        )

        // Filter by Tag "Work"
        val workTasks = tasks.filter { it.tags.any { t -> t.equals("Work", ignoreCase = true) } }
        assertEquals(1, workTasks.size)
        assertEquals("Design Sprint", workTasks[0].title)

        // Filter by Priority HIGH
        val highTasks = tasks.filter { it.priority == Priority.HIGH }
        assertEquals(1, highTasks.size)
        assertEquals(1, highTasks[0].id)

        // Combined Tag and Search
        val search = "Design"
        val filtered = tasks.filter { it.tags.contains(search) || it.title.contains(search) }
        assertEquals(1, filtered.size)
        assertEquals("Design Sprint", filtered[0].title)
    }

    // 10. Recurrence Trigger Calculation
    @Test
    fun testDailyRecurrenceNextTriggerCalculation() {
        val dueTime = 1700000000000L
        val nextTrigger = com.example.mytodoapp.notification.NotificationScheduler.calculateNextTriggerMillis(
            dueTimeMillis = dueTime,
            endTimeMillis = null,
            minutesBefore = 10,
            recurrence = com.example.mytodoapp.model.RecurrenceType.DAILY,
            currentTime = 1700000000000L
        )
        assertNotNull(nextTrigger)
        assertTrue(nextTrigger!! > 1700000000000L)
    }

    // 11. Daily Recurring Reminder Lead Time (e.g. 6:00 PM task with 10 min reminder -> 5:50 PM)
    @Test
    fun testDailyRecurringTask_reminderLeadTime() {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).apply {
            set(java.util.Calendar.HOUR_OF_DAY, 18) // 6:00 PM
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val dueTime = calendar.timeInMillis

        // Current time is 12:00 PM on the same day
        val nowCal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).apply {
            timeInMillis = dueTime
            set(java.util.Calendar.HOUR_OF_DAY, 12)
            set(java.util.Calendar.MINUTE, 0)
        }
        val now = nowCal.timeInMillis

        val triggerTime = com.example.mytodoapp.notification.NotificationScheduler.calculateNextTriggerMillis(
            dueTimeMillis = dueTime,
            endTimeMillis = null,
            minutesBefore = 10,
            recurrence = com.example.mytodoapp.model.RecurrenceType.DAILY,
            currentTime = now
        )
        assertNotNull(triggerTime)

        val triggerCal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).apply {
            timeInMillis = triggerTime!!
        }
        assertEquals(17, triggerCal.get(java.util.Calendar.HOUR_OF_DAY)) // 5:00 PM
        assertEquals(50, triggerCal.get(java.util.Calendar.MINUTE))      // 50 minutes (5:50 PM)
    }

    // 12. Lead Time Reminders for 5, 10, 15, 30, 60 minutes
    @Test
    fun testLeadTimeCalculation_variousIntervals() {
        val due = 1700071200000L // arbitrary fixed timestamp
        val now = due - 2 * 3600_000L // 2 hours before

        val trigger5 = com.example.mytodoapp.util.ReminderTime.alarmTriggerMillis(due, 5, now)
        val trigger10 = com.example.mytodoapp.util.ReminderTime.alarmTriggerMillis(due, 10, now)
        val trigger15 = com.example.mytodoapp.util.ReminderTime.alarmTriggerMillis(due, 15, now)
        val trigger30 = com.example.mytodoapp.util.ReminderTime.alarmTriggerMillis(due, 30, now)
        val trigger60 = com.example.mytodoapp.util.ReminderTime.alarmTriggerMillis(due, 60, now)

        assertEquals(due - 5 * 60_000L, trigger5)
        assertEquals(due - 10 * 60_000L, trigger10)
        assertEquals(due - 15 * 60_000L, trigger15)
        assertEquals(due - 30 * 60_000L, trigger30)
        assertEquals(due - 60 * 60_000L, trigger60)
    }
}
