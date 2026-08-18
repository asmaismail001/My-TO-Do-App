package com.example.mytodoapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.repository.TodoRepository
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    var todoList by mutableStateOf<List<Todo>>(emptyList())
        private set

    init {
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            todoList = repository.getTodos()
        }
    }

    fun addTodo(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addTodo(title.trim())
            loadTodos()
        }
    }

    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            repository.toggleTodo(todo)
            loadTodos()
        }
    }

    fun updateTodo(todo: Todo, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateTodo(todo, newTitle.trim())
            loadTodos()
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
            loadTodos()
        }
    }
}

class TodoViewModelFactory(
    private val repository: TodoRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TodoViewModel(repository) as T
    }
}