package com.example.mytodoapp.repository

import android.content.Context
import com.example.mytodoapp.model.Todo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class TodoRepository(private val context: Context) {

    fun getTodos(): List<Todo> {
        val jsonString = readJsonFromAssets("todos.json")
        return parseJson(jsonString)
    }

    private fun readJsonFromAssets(fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }

    private fun parseJson(jsonString: String): List<Todo> {
        val listType = object : TypeToken<List<Todo>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }
}