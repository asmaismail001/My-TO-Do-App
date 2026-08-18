package com.example.mytodoapp.repository

import android.content.Context
import com.example.mytodoapp.model.Todo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TodoRepository(private val context: Context) {

    private val dao = AppDatabase.getDatabase(context).todoDao()

    suspend fun getTodos(): List<Todo> {
        migrateFromJsonIfNeeded()
        return dao.getAllTodos()
    }

    suspend fun addTodo(title: String) {
        dao.insertTodo(Todo(title = title, completed = false))
    }

    suspend fun toggleTodo(todo: Todo) {
        dao.updateTodo(todo.copy(completed = !todo.completed))
    }

    suspend fun updateTodo(todo: Todo, newTitle: String) {
        dao.updateTodo(todo.copy(title = newTitle))
    }

    suspend fun deleteTodo(todo: Todo) {
        dao.deleteTodo(todo)
    }


    private suspend fun migrateFromJsonIfNeeded() {
        if (dao.getCount() == 0) {
            try {
                val jsonString = context.assets.open("todos.json").bufferedReader().use { it.readText() }
                val listType = object : TypeToken<List<Todo>>() {}.type
                val jsonTodos: List<Todo> = Gson().fromJson(jsonString, listType)
                jsonTodos.forEach { dao.insertTodo(Todo(title = it.title, completed = it.completed)) }
            } catch (e: Exception) {

            }
        }
    }
}