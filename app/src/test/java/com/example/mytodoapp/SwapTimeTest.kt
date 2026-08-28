package com.example.mytodoapp

import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.util.TimeConflict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SwapTimeTest {

    private fun createTodo(
        id: Int,
        start: Long,
        end: Long,
        title: String = "Task $id",
        completed: Boolean = false,
        priority: Priority = Priority.MEDIUM,
        description: String = "desc $id",
        notificationMinutesBefore: Int = 10
    ): Todo {
        return Todo(
            id = id,
            title = title,
            description = description,
            priority = priority,
            dueTimeMillis = start,
            endTimeMillis = end,
            completed = completed,
            notificationMinutesBefore = notificationMinutesBefore
        )
    }

    private fun swapped(source: Todo, other: Todo): Pair<Todo, Todo> {
        return source.copy(
            dueTimeMillis = other.dueTimeMillis,
            endTimeMillis = other.endTimeMillis
        ) to other.copy(
            dueTimeMillis = source.dueTimeMillis,
            endTimeMillis = source.endTimeMillis
        )
    }

    @Test
    fun adjacentSlotsCanSwapWithoutConflict() {
        val meeting = createTodo(1, 5000L, 6000L, title = "Meeting")
        val important = createTodo(2, 6000L, 7000L, title = "Important Task", priority = Priority.HIGH)
        val todos = listOf(meeting, important)

        assertNull(TimeConflict.findSwapConflict(important, meeting, todos))

        val (newImportant, newMeeting) = swapped(important, meeting)
        assertEquals(5000L, newImportant.dueTimeMillis)
        assertEquals(6000L, newImportant.endTimeMillis)
        assertEquals("Important Task", newImportant.title)
        assertEquals(Priority.HIGH, newImportant.priority)
        assertEquals("desc 2", newImportant.description)

        assertEquals(6000L, newMeeting.dueTimeMillis)
        assertEquals(7000L, newMeeting.endTimeMillis)
        assertEquals("Meeting", newMeeting.title)
    }

    @Test
    fun swapBlockedWhenThirdTaskOccupiesTargetSlot() {
        val taskA = createTodo(1, 5000L, 6000L)
        val taskB = createTodo(2, 6000L, 8000L)
        val taskC = createTodo(3, 7000L, 8000L)
        val todos = listOf(taskA, taskB, taskC)

        val conflict = TimeConflict.findSwapConflict(taskA, taskB, todos)
        assertNotNull(conflict)
        assertEquals(3, conflict!!.id)
    }

    @Test
    fun swapOfAdjacentSlotsIgnoresThirdTaskAfterThePair() {
        val taskA = createTodo(1, 5000L, 6000L)
        val taskB = createTodo(2, 6000L, 7000L)
        val taskC = createTodo(3, 7000L, 8000L)
        val todos = listOf(taskA, taskB, taskC)

        assertNull(TimeConflict.findSwapConflict(taskA, taskB, todos))
    }

    @Test
    fun normalEditStillBlockedAfterSwapLogicExists() {
        val meeting = createTodo(1, 5000L, 6000L, title = "Meeting")
        val important = createTodo(2, 6000L, 7000L, title = "Important Task")
        val todos = listOf(meeting, important)

        val conflict = TimeConflict.findOverlappingTask(
            startTime = 5000L,
            endTime = 6000L,
            todos = todos,
            excludeTaskId = 2
        )
        assertNotNull(conflict)
        assertEquals(1, conflict!!.id)
    }

    @Test
    fun completedTaskDoesNotBlockASwap() {
        val taskA = createTodo(1, 5000L, 6000L)
        val taskB = createTodo(2, 6000L, 8000L)
        val completed = createTodo(3, 7000L, 8000L, completed = true)
        val todos = listOf(taskA, taskB, completed)

        assertNull(TimeConflict.findSwapConflict(taskA, taskB, todos))
    }

    @Test
    fun crossDateSlotsAreExchangedInFull() {
        val day1Start = 1_000_000L
        val day1End = 1_003_600_000L
        val day2Start = 86_400_000L + 1_000_000L
        val day2End = 86_400_000L + 1_003_600_000L
        val taskA = createTodo(1, day1Start, day1End)
        val taskB = createTodo(2, day2Start, day2End)

        val (newA, newB) = swapped(taskA, taskB)
        assertEquals(day2Start, newA.dueTimeMillis)
        assertEquals(day2End, newA.endTimeMillis)
        assertEquals(day1Start, newB.dueTimeMillis)
        assertEquals(day1End, newB.endTimeMillis)
        assertEquals(taskA.notificationMinutesBefore, newA.notificationMinutesBefore)
        assertEquals(taskB.priority, newB.priority)
    }
}
