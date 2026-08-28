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
    ): Todo? = findOverlappingTask(startTime, endTime, todos, setOf(excludeTaskId))

    fun findOverlappingTask(
        startTime: Long?,
        endTime: Long?,
        todos: List<Todo>,
        excludeTaskIds: Set<Int>
    ): Todo? {
        if (startTime == null || endTime == null) return null
        return todos.firstOrNull { todo ->
            !todo.completed &&
                todo.id !in excludeTaskIds &&
                todo.dueTimeMillis != null &&
                todo.endTimeMillis != null &&
                startTime < todo.endTimeMillis &&
                todo.dueTimeMillis < endTime
        }
    }

    /**
     * After exchanging [taskA] and [taskB] time slots, returns a third task that
     * would overlap either new slot. The pair itself is excluded so they can
     * occupy each other's existing slots.
     */
    fun findSwapConflict(taskA: Todo, taskB: Todo, todos: List<Todo>): Todo? {
        val exclude = setOf(taskA.id, taskB.id)
        findOverlappingTask(
            taskB.dueTimeMillis,
            taskB.endTimeMillis,
            todos,
            exclude
        )?.let { return it }
        return findOverlappingTask(
            taskA.dueTimeMillis,
            taskA.endTimeMillis,
            todos,
            exclude
        )
    }
}
