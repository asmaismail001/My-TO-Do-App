package com.example.mytodoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.viewmodel.TodoViewModel

// Color palette
private val BackgroundColor = Color(0xFFF7F5FB)
private val PrimaryPurple = Color(0xFF6C5DD3)
private val CardCompleted = Color(0xFFE6F4EA)
private val CardPending = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF2A2A3B)
private val TextGrey = Color(0xFF8A8A9E)
private val DeleteRed = Color(0xFFE57373)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskText by remember { mutableStateOf("") }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<Todo?>(null) }
    var editText by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingTodo by remember { mutableStateOf<Todo?>(null) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("My To-Do App", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryPurple,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        val completedCount = viewModel.todoList.count { it.completed }
        val total = viewModel.todoList.size

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "$completedCount of $total completed",
                color = TextGrey,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                items(viewModel.todoList, key = { it.id }) { todo ->
                    TodoItemRow(
                        todo = todo,
                        onToggle = { viewModel.toggleTodo(todo) },
                        onEditClick = {
                            editingTodo = todo
                            editText = todo.title
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

    // Add Task Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newTaskText = "" },
            title = { Text("New Task", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = { Text("What do you need to do?") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addTodo(newTaskText)
                    newTaskText = ""
                    showAddDialog = false
                }) {
                    Text("Add", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newTaskText = "" }) {
                    Text("Cancel", color = TextGrey)
                }
            }
        )
    }

    // Edit Task Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false; editingTodo = null },
            title = { Text("Edit Task", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    placeholder = { Text("Update task title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editingTodo?.let { viewModel.updateTodo(it, editText) }
                    showEditDialog = false
                    editingTodo = null
                }) {
                    Text("Update", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false; editingTodo = null }) {
                    Text("Cancel", color = TextGrey)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; deletingTodo = null },
            title = { Text("Delete Task?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${deletingTodo?.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    deletingTodo?.let { viewModel.deleteTodo(it) }
                    showDeleteDialog = false
                    deletingTodo = null
                }) {
                    Text("Delete", color = DeleteRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; deletingTodo = null }) {
                    Text("Cancel", color = TextGrey)
                }
            }
        )
    }
}

@Composable
fun TodoItemRow(
    todo: Todo,
    onToggle: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onEditClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.completed) CardCompleted else CardPending
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    fontWeight = FontWeight.Medium,
                    color = TextDark,
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = if (todo.completed) "Completed" else "Pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (todo.completed) Color(0xFF3AA76D) else TextGrey
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Task", tint = DeleteRed)
            }
        }
    }
}