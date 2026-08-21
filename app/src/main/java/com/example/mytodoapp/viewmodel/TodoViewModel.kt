package com.example.mytodoapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.notification.NotificationScheduler
import com.example.mytodoapp.repository.TodoRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class TodoViewModel(
    private val repository: TodoRepository,
    private val appContext: Context
) : ViewModel() {

    var todoList by mutableStateOf<List<Todo>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    val allTasks: List<Todo>
        get() = applySearch(todoList)

    val completedTasks: List<Todo>
        get() = applySearch(todoList.filter { it.completed })

    val pendingTasks: List<Todo>
        get() = applySearch(todoList.filter { !it.completed })

    init {
        loadTodos()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    private fun applySearch(list: List<Todo>): List<Todo> {
        if (searchQuery.isBlank()) return list
        val query = searchQuery.trim().lowercase()
        return list.filter {
            it.title.lowercase().contains(query) ||
                    it.description.lowercase().contains(query) ||
                    it.priority.name.lowercase().contains(query)
        }
    }

    private fun loadTodos() {
        viewModelScope.launch {
            todoList = repository.getTodos()
        }
    }

    fun addTodo(title: String, description: String, priority: Priority, dueTimeMillis: Long?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val savedTodo = repository.addTodo(title.trim(), description.trim(), priority, dueTimeMillis)
            if (dueTimeMillis != null) {
                NotificationScheduler.scheduleReminder(appContext, savedTodo.id, savedTodo.title, dueTimeMillis)
            }
            loadTodos()
        }
    }

    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            repository.toggleTodo(todo)
            if (!todo.completed) {
                NotificationScheduler.cancelReminder(appContext, todo.id)
            }
            loadTodos()
        }
    }

    fun updateTodo(
        todo: Todo,
        newTitle: String,
        newDescription: String,
        newPriority: Priority,
        newDueTimeMillis: Long?
    ) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateTodo(todo, newTitle.trim(), newDescription.trim(), newPriority, newDueTimeMillis)
            NotificationScheduler.cancelReminder(appContext, todo.id)
            if (newDueTimeMillis != null) {
                NotificationScheduler.scheduleReminder(appContext, todo.id, newTitle.trim(), newDueTimeMillis)
            }
            loadTodos()
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            NotificationScheduler.cancelReminder(appContext, todo.id)
            repository.deleteTodo(todo)
            loadTodos()
        }
    }

    fun exportTasks(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            try {
                val json = Gson().toJson(todoList)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
            } catch (e: Exception) {
                // export failed silently
            }
        }
    }

    fun importTasks(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }
                if (json != null) {
                    val listType = object : TypeToken<List<Todo>>() {}.type
                    val imported: List<Todo> = Gson().fromJson(json, listType)
                    imported.forEach {
                        repository.addTodo(it.title, it.description, it.priority, it.dueTimeMillis)
                    }
                    loadTodos()
                }
            } catch (e: Exception) {
                // import failed silently
            }
        }
    }
}

class TodoViewModelFactory(
    private val repository: TodoRepository,
    private val appContext: Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TodoViewModel(repository, appContext) as T
    }
}