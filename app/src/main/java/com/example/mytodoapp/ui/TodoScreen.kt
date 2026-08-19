package com.example.mytodoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.components.AddTaskDialog
import com.example.mytodoapp.ui.components.DeleteTaskDialog
import com.example.mytodoapp.ui.components.EditTaskDialog
import com.example.mytodoapp.ui.components.TodoItemRow
import com.example.mytodoapp.ui.components.TodoSearchBar
import com.example.mytodoapp.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var newDueTime by remember { mutableStateOf<Long?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<Todo?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var editDueTime by remember { mutableStateOf<Long?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingTodo by remember { mutableStateOf<Todo?>(null) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("My To-Do App", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SalmonPink,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SageGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        val list = viewModel.filteredList
        val completedCount = viewModel.todoList.count { it.completed }
        val total = viewModel.todoList.size

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TodoSearchBar(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$total tasks",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$completedCount completed",
                    color = SuccessGreen,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                items(list, key = { it.id }) { todo ->
                    TodoItemRow(
                        todo = todo,
                        onToggle = { viewModel.toggleTodo(todo) },
                        onEditClick = {
                            editingTodo = todo
                            editTitle = todo.title
                            editDescription = todo.description
                            editPriority = todo.priority
                            editDueTime = todo.dueTimeMillis
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            deletingTodo = todo
                            showDeleteDialog = true
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            title = newTitle,
            onTitleChange = { newTitle = it },
            description = newDescription,
            onDescriptionChange = { newDescription = it },
            priority = newPriority,
            onPriorityChange = { newPriority = it },
            dueTimeMillis = newDueTime,
            onDueTimeChange = { newDueTime = it },
            onConfirm = {
                viewModel.addTodo(newTitle, newDescription, newPriority, newDueTime)
                newTitle = ""
                newDescription = ""
                newPriority = Priority.MEDIUM
                newDueTime = null
                showAddDialog = false
            },
            onDismiss = {
                showAddDialog = false
                newTitle = ""
                newDescription = ""
                newPriority = Priority.MEDIUM
                newDueTime = null
            }
        )
    }

    if (showEditDialog) {
        EditTaskDialog(
            title = editTitle,
            onTitleChange = { editTitle = it },
            description = editDescription,
            onDescriptionChange = { editDescription = it },
            priority = editPriority,
            onPriorityChange = { editPriority = it },
            dueTimeMillis = editDueTime,
            onDueTimeChange = { editDueTime = it },
            onConfirm = {
                editingTodo?.let { viewModel.updateTodo(it, editTitle, editDescription, editPriority, editDueTime) }
                showEditDialog = false
                editingTodo = null
            },
            onDismiss = {
                showEditDialog = false
                editingTodo = null
            }
        )
    }

    if (showDeleteDialog) {
        DeleteTaskDialog(
            taskTitle = deletingTodo?.title ?: "",
            onConfirm = {
                deletingTodo?.let { viewModel.deleteTodo(it) }
                showDeleteDialog = false
                deletingTodo = null
            },
            onDismiss = {
                showDeleteDialog = false
                deletingTodo = null
            }
        )
    }
}