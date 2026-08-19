package com.example.mytodoapp.repository

import android.content.Context
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo

class TodoRepository(private val context: Context) {

    private val dao = AppDatabase.getDatabase(context).todoDao()

    suspend fun getTodos(): List<Todo> = dao.getAllTodos()

    suspend fun addTodo(
        title: String,
        description: String,
        priority: Priority,
        dueTimeMillis: Long?
    ): Todo {
        val todo = Todo(
            title = title,
            description = description,
            priority = priority,
            completed = false,
            createdAt = System.currentTimeMillis(),
            dueTimeMillis = dueTimeMillis
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
        newDueTimeMillis: Long?
    ) {
        dao.updateTodo(
            todo.copy(
                title = newTitle,
                description = newDescription,
                priority = newPriority,
                dueTimeMillis = newDueTimeMillis
            )
        )
    }

    suspend fun deleteTodo(todo: Todo) {
        dao.deleteTodo(todo)
    }
}