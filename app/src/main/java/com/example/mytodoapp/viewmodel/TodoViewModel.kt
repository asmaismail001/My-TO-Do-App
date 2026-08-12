package com.example.mytodoapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.repository.TodoRepository

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    var todoList by mutableStateOf<List<Todo>>(emptyList())
        private set

    init {
        loadTodos()
    }

    private fun loadTodos() {
        todoList = repository.getTodos()
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