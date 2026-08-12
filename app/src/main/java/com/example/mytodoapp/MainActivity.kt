package com.example.mytodoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytodoapp.repository.TodoRepository
import com.example.mytodoapp.ui.TodoScreen
import com.example.mytodoapp.viewmodel.TodoViewModel
import com.example.mytodoapp.viewmodel.TodoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val repository = TodoRepository(applicationContext)
                    val viewModel: TodoViewModel = viewModel(
                        factory = TodoViewModelFactory(repository)
                    )
                    TodoScreen(viewModel = viewModel)
                }
            }
        }
    }
}