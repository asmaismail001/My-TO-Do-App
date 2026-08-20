package com.example.mytodoapp.ui

import androidx.compose.foundation.layout.*
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
import com.example.mytodoapp.ui.components.BottomNavBar
import com.example.mytodoapp.ui.components.CalendarView
import com.example.mytodoapp.ui.components.DeleteTaskDialog
import com.example.mytodoapp.ui.components.EditTaskDialog
import com.example.mytodoapp.ui.components.TaskListContent
import com.example.mytodoapp.ui.components.TodoSearchBar
import com.example.mytodoapp.util.CalendarUtil
import com.example.mytodoapp.viewmodel.TodoViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.ALL) }

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

    var selectedDay by remember { mutableStateOf(Calendar.getInstance()) }
    var visibleMonth by remember { mutableStateOf(Calendar.getInstance()) }

    fun openEdit(todo: Todo) {
        editingTodo = todo
        editTitle = todo.title
        editDescription = todo.description
        editPriority = todo.priority
        editDueTime = todo.dueTimeMillis
        showEditDialog = true
    }

    fun openDelete(todo: Todo) {
        deletingTodo = todo
        showDeleteDialog = true
    }

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
        bottomBar = {
            BottomNavBar(selected = currentScreen, onSelect = { currentScreen = it })
        },
        floatingActionButton = {
            if (currentScreen != Screen.CALENDAR) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = SageGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                Screen.ALL -> {
                    TodoSearchBar(
                        query = viewModel.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) }
                    )
                    val list = viewModel.allTasks
                    val completedCount = viewModel.todoList.count { it.completed }
                    val total = viewModel.todoList.size

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

                    TaskListContent(
                        tasks = list,
                        emptyMessage = "No tasks yet. Tap + to add one.",
                        onToggle = { viewModel.toggleTodo(it) },
                        onEditClick = { openEdit(it) },
                        onDeleteClick = { openDelete(it) }
                    )
                }

                Screen.COMPLETED -> {
                    TodoSearchBar(
                        query = viewModel.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) }
                    )
                    TaskListContent(
                        tasks = viewModel.completedTasks,
                        emptyMessage = "No completed tasks yet.",
                        onToggle = { viewModel.toggleTodo(it) },
                        onEditClick = { openEdit(it) },
                        onDeleteClick = { openDelete(it) }
                    )
                }

                Screen.DUE -> {
                    TodoSearchBar(
                        query = viewModel.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) }
                    )
                    TaskListContent(
                        tasks = viewModel.dueTasks,
                        emptyMessage = "No tasks with a due date.",
                        onToggle = { viewModel.toggleTodo(it) },
                        onEditClick = { openEdit(it) },
                        onDeleteClick = { openDelete(it) }
                    )
                }

                Screen.CALENDAR -> {
                    CalendarView(
                        tasks = viewModel.todoList,
                        selectedDay = selectedDay,
                        onDaySelected = { selectedDay = it },
                        visibleMonth = visibleMonth,
                        onMonthChange = { visibleMonth = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val tasksForSelectedDay = viewModel.todoList.filter {
                        it.dueTimeMillis != null && CalendarUtil.isSameDay(it.dueTimeMillis, selectedDay.timeInMillis)
                    }

                    TaskListContent(
                        tasks = tasksForSelectedDay,
                        emptyMessage = "No tasks on this day.",
                        onToggle = { viewModel.toggleTodo(it) },
                        onEditClick = { openEdit(it) },
                        onDeleteClick = { openDelete(it) }
                    )
                }
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
            createdAt = editingTodo?.createdAt ?: System.currentTimeMillis(),
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