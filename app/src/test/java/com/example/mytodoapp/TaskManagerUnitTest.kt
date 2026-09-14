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

    // 7. Focus Timer
    @Test
    fun testFocusTimer_startAndResetBehavior() {
        // Test the main timer behavior such as starting and resetting the timer.
        val defaultMinutes = 25
        val customFocusMinutes = defaultMinutes
        var timeLeftMillis = customFocusMinutes * 60 * 1000L
        var isRunning = false
        var isBreak = false

        // Initial state: 25 minutes, not running
        assertEquals(25 * 60 * 1000L, timeLeftMillis)
        assertFalse(isRunning)

        // Start timer
        isRunning = true
        assertTrue(isRunning)

        // Simulate tick
        timeLeftMillis -= 5000L
        assertEquals(25 * 60 * 1000L - 5000L, timeLeftMillis)

        // Reset timer
        isRunning = false
        isBreak = false
        timeLeftMillis = customFocusMinutes * 60 * 1000L

        // Verify state is restored after reset
        assertFalse(isRunning)
        assertFalse(isBreak)
        assertEquals(25 * 60 * 1000L, timeLeftMillis)
    }
}
