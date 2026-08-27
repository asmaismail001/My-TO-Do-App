package com.example.mytodoapp

import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.util.TimeConflict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConflictValidationTest {

    private fun checkTimeConflict(
        startTime: Long?,
        endTime: Long?,
        excludeTaskId: Int = 0,
        todoList: List<Todo>
    ): Todo? {
        return TimeConflict.findOverlappingTask(startTime, endTime, todoList, excludeTaskId)
    }

    private fun createTodo(id: Int, start: Long, end: Long, completed: Boolean = false): Todo {
        return Todo(
            id = id,
            title = "Task $id",
            dueTimeMillis = start,
            endTimeMillis = end,
            completed = completed
        )
    }

    @Test
    fun testConflictValidation() {
        // Task A: 5000 to 6000 (representing 5:00 - 6:00)
        val existingTasks = listOf(
            createTodo(1, 5000L, 6000L)
        )

        // 1. Same slot: 5:00 - 6:00 (blocked)
        assertNotNull(checkTimeConflict(5000L, 6000L, todoList = existingTasks))

        // 2. Overlap end: 5:30 - 6:30 (blocked)
        assertNotNull(checkTimeConflict(5500L, 6500L, todoList = existingTasks))

        // 3. Overlap start: 4:30 - 5:30 (blocked)
        assertNotNull(checkTimeConflict(4500L, 5500L, todoList = existingTasks))

        // 4. Overlap complete: 4:00 - 7:00 (blocked)
        assertNotNull(checkTimeConflict(4000L, 7000L, todoList = existingTasks))

        // 5. Overlap inside: 5:15 - 5:45 (blocked)
        assertNotNull(checkTimeConflict(5150L, 5450L, todoList = existingTasks))

        // 6. Before: 4:00 - 5:00 (allowed)
        assertNull(checkTimeConflict(4000L, 5000L, todoList = existingTasks))

        // 7. After: 6:00 - 7:00 (allowed)
        assertNull(checkTimeConflict(6000L, 7000L, todoList = existingTasks))

        // 8. Self-editing: exclude ID = 1 (allowed)
        assertNull(checkTimeConflict(5000L, 6000L, excludeTaskId = 1, todoList = existingTasks))
    }

    @Test
    fun completedTaskDoesNotBlockTheSameSlot() {
        val existingTasks = listOf(
            createTodo(1, 5000L, 6000L, completed = true)
        )

        assertNull(checkTimeConflict(5000L, 6000L, todoList = existingTasks))
        assertNull(checkTimeConflict(5500L, 6500L, todoList = existingTasks))
    }

    @Test
    fun completedOverlapIsSkippedInFavorOfPendingConflict() {
        val existingTasks = listOf(
            createTodo(1, 5000L, 6000L, completed = true),
            createTodo(2, 5500L, 6500L, completed = false)
        )

        val conflict = checkTimeConflict(5000L, 6000L, todoList = existingTasks)
        assertNotNull(conflict)
        assertEquals(2, conflict!!.id)
    }
}
