package com.example.mytodoapp.repository

import android.content.Context
import com.example.mytodoapp.model.Todo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class TodoRepository(private val context: Context) {

    private val fileName = "todos.json"
    private val internalFile = File(context.filesDir, fileName)

    fun getTodos(): List<Todo> {
        ensureFileExists()
        val jsonString = internalFile.readText()
        return parseJson(jsonString)
    }

    fun addTodo(title: String) {
        val currentList = getTodos().toMutableList()
        val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        currentList.add(Todo(id = newId, title = title, completed = false))
        saveTodos(currentList)
    }

    fun toggleTodo(id: Int) {
        val currentList = getTodos().map {
            if (it.id == id) it.copy(completed = !it.completed) else it
        }
        saveTodos(currentList)
    }

    fun updateTodo(id: Int, newTitle: String) {
        val currentList = getTodos().map {
            if (it.id == id) it.copy(title = newTitle) else it
        }
        saveTodos(currentList)
    }
    fun deleteTodo(id: Int) {
        val currentList = getTodos().filter { it.id != id }
        saveTodos(currentList)
    }

    private fun saveTodos(list: List<Todo>) {
        internalFile.writeText(Gson().toJson(list))
    }

    private fun ensureFileExists() {
        if (!internalFile.exists()) {
            val assetText = context.assets.open(fileName).bufferedReader().use { it.readText() }
            internalFile.writeText(assetText)
        }
    }

    private fun parseJson(jsonString: String): List<Todo> {
        val listType = object : TypeToken<List<Todo>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }
}