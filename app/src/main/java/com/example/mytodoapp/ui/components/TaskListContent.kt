package com.example.mytodoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.textMutedFor

@Composable
fun TaskListContent(
    tasks: List<Todo>,
    emptyMessage: String,
    onToggle: (Todo) -> Unit,
    onEditClick: (Todo) -> Unit,
    onDeleteClick: (Todo) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    if (tasks.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emptyMessage, color = textMutedFor(isDark), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        items(tasks, key = { it.id }) { todo ->
            TodoItemRow(
                todo = todo,
                onToggle = { onToggle(todo) },
                onEditClick = { onEditClick(todo) },
                onDeleteClick = { onDeleteClick(todo) }
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}