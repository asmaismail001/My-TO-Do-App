package com.example.mytodoapp

import com.example.mytodoapp.model.Todo
import org.junit.Test
import org.junit.Assert.*

class ConflictValidationTest {

    private fun checkTimeConflict(
        startTime: Long?,
        endTime: Long?,
        excludeTaskId: Int = 0,
        todoList: List<Todo>
    ): Todo? {
        if (startTime == null || endTime == null) return null
        return todoList.firstOrNull { todo ->
            todo.id != excludeTaskId &&
            todo.dueTimeMillis != null &&
            todo.endTimeMillis != null &&
            startTime < todo.endTimeMillis &&
            todo.dueTimeMillis < endTime
        }
    }

    private fun createTodo(id: Int, start: Long, end: Long): Todo {
        return Todo(
            id = id,
            title = "Task $id",
            dueTimeMillis = start,
            endTimeMillis = end,
            completed = false
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
}
