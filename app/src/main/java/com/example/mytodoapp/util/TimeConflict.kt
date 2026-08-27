package com.example.mytodoapp.util

import com.example.mytodoapp.model.Todo

object TimeConflict {

    /**
     * Returns the first incomplete task whose [Todo.dueTimeMillis]–[Todo.endTimeMillis]
     * range overlaps [[startTime], [endTime]). Completed tasks do not occupy a slot.
     */
    fun findOverlappingTask(
        startTime: Long?,
        endTime: Long?,
        todos: List<Todo>,
        excludeTaskId: Int = 0
    ): Todo? {
        if (startTime == null || endTime == null) return null
        return todos.firstOrNull { todo ->
            !todo.completed &&
                todo.id != excludeTaskId &&
                todo.dueTimeMillis != null &&
                todo.endTimeMillis != null &&
                startTime < todo.endTimeMillis &&
                todo.dueTimeMillis < endTime
        }
    }
}
