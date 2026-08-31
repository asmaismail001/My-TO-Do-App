package com.example.mytodoapp.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo

class TodoRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val dao = database.todoDao()

    suspend fun getTodos(userId: String? = null): List<Todo> {
        return if (userId != null) {
            dao.getAllTodosForUser(userId)
        } else {
            dao.getAllTodos()
        }
    }

    suspend fun claimOrphanTasks(userId: String) {
        dao.assignOrphanTasksToUser(userId)
    }

    suspend fun addTodo(
        title: String,
        description: String,
        priority: Priority,
        dueTimeMillis: Long?,
        endTimeMillis: Long? = null,
        attachmentUri: String? = null,
        notificationEnabled: Boolean = false,
        notificationMinutesBefore: Int = 10,
        userId: String? = null
    ): Todo {
        val todo = Todo(
            title = title,
            description = description,
            priority = priority,
            completed = false,
            createdAt = System.currentTimeMillis(),
            dueTimeMillis = dueTimeMillis,
            endTimeMillis = endTimeMillis,
            attachmentUri = attachmentUri,
            notificationEnabled = notificationEnabled,
            notificationMinutesBefore = notificationMinutesBefore,
            userId = userId
        )
        val newId = dao.insertTodo(todo)
        return todo.copy(id = newId.toInt())
    }

    suspend fun toggleTodo(todo: Todo) {
        dao.updateTodo(todo.copy(completed = !todo.completed))
    }

    suspend fun updateTodo(
        todo: Todo,
        newTitle: String,
        newDescription: String,
        newPriority: Priority,
        newDueTimeMillis: Long?,
        newEndTimeMillis: Long?,
        newAttachmentUri: String? = todo.attachmentUri,
        newNotificationEnabled: Boolean = todo.notificationEnabled,
        newNotificationMinutesBefore: Int = todo.notificationMinutesBefore
    ) {
        dao.updateTodo(
            todo.copy(
                title = newTitle,
                description = newDescription,
                priority = newPriority,
                dueTimeMillis = newDueTimeMillis,
                endTimeMillis = newEndTimeMillis,
                attachmentUri = newAttachmentUri,
                notificationEnabled = newNotificationEnabled,
                notificationMinutesBefore = newNotificationMinutesBefore
            )
        )
    }

    suspend fun deleteTodo(todo: Todo) {
        dao.deleteTodo(todo)
    }

    suspend fun swapTodoTimes(first: Todo, second: Todo) {
        database.withTransaction {
            dao.updateTodo(first)
            dao.updateTodo(second)
        }
    }
}