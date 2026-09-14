package com.example.mytodoapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.FocusTimerDialog
import com.example.mytodoapp.ui.Screen
import com.example.mytodoapp.ui.components.AddTaskDialog
import com.example.mytodoapp.ui.components.BottomNavBar
import com.example.mytodoapp.ui.components.DeleteTaskDialog
import com.example.mytodoapp.ui.components.TaskDetailsScreen
import com.example.mytodoapp.ui.components.TaskListContent
import com.example.mytodoapp.ui.components.TodoItemRow
import com.example.mytodoapp.ui.components.TodoSearchBar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskManagerUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // 1. Add Task Flow
    @Test
    fun testAddTaskFlow() {
        var showDialog by mutableStateOf(true)
        var title by mutableStateOf("")
        var priority by mutableStateOf(Priority.MEDIUM)
        val taskList = mutableStateListOf<Todo>()

        composeTestRule.setContent {
            Column {
                if (showDialog) {
                    AddTaskDialog(
                        title = title,
                        onTitleChange = { title = it },
                        description = "",
                        onDescriptionChange = {},
                        priority = priority,
                        onPriorityChange = { priority = it },
                        dueTimeMillis = null,
                        onDueTimeChange = {},
                        endTimeMillis = null,
                        onEndTimeChange = {},
                        notificationEnabled = false,
                        onNotificationEnabledChange = {},
                        notificationMinutesBefore = 10,
                        onNotificationMinutesBeforeChange = {},
                        attachmentUri = null,
                        onAttachmentChange = {},
                        onConfirm = {
                            taskList.add(Todo(id = 1, title = title, priority = priority, completed = false))
                            showDialog = false
                        },
                        onDismiss = { showDialog = false }
                    )
                }
                TaskListContent(
                    tasks = taskList,
                    emptyMessage = "No tasks found",
                    onToggle = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onFocusClick = {},
                    onTodoClick = {}
                )
            }
        }

        // Verify Add Task dialog is displayed
        composeTestRule.onNodeWithText("New Task").assertIsDisplayed()

        // Enter task title
        composeTestRule.onNodeWithText("Title").performTextInput("Buy Groceries")

        // Select priority
        composeTestRule.onNodeWithText("Medium").performClick()
        composeTestRule.onNodeWithText("High").performClick()

        // Save the task
        composeTestRule.onNodeWithText("Add Task").performClick()

        // Verify that the task appears in the task list
        composeTestRule.onNodeWithText("Buy Groceries").assertIsDisplayed()
    }

    // 2. Complete Task Flow
    @Test
    fun testCompleteTaskFlow() {
        // Select a task, mark it as completed, and verify the completed state in the UI.
        var isCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            Column {
                Text(text = if (isCompleted) "Status: Completed" else "Status: Pending")
                TodoItemRow(
                    todo = Todo(id = 1, title = "Submit Report", completed = isCompleted),
                    onToggle = { isCompleted = !isCompleted },
                    onEditClick = {},
                    onDeleteClick = {},
                    onFocusClick = {},
                    onTodoClick = {}
                )
            }
        }

        // Verify initial pending state in the UI
        composeTestRule.onNodeWithText("Submit Report").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status: Pending").assertIsDisplayed()

        // Mark it as completed
        composeTestRule.onAllNodes(hasClickAction())[1].performClick()

        // Verify the completed state in the UI
        composeTestRule.onNodeWithText("Status: Completed").assertIsDisplayed()
    }

    // 3. Delete Task Flow
    @Test
    fun testDeleteTaskFlow() {
        // Delete a task and verify that it is removed from the UI.
        var deletingTodo by mutableStateOf<Todo?>(null)
        val taskList = mutableStateListOf(
            Todo(id = 1, title = "Task to Delete", completed = false)
        )

        composeTestRule.setContent {
            Column {
                TaskListContent(
                    tasks = taskList,
                    emptyMessage = "No tasks found",
                    onToggle = {},
                    onEditClick = {},
                    onDeleteClick = { deletingTodo = it },
                    onFocusClick = {},
                    onTodoClick = {}
                )
                deletingTodo?.let { todo ->
                    DeleteTaskDialog(
                        taskTitle = todo.title,
                        onConfirm = {
                            taskList.removeAll { it.id == todo.id }
                            deletingTodo = null
                        },
                        onDismiss = { deletingTodo = null }
                    )
                }
            }
        }

        // Verify task is initially displayed
        composeTestRule.onNodeWithText("Task to Delete").assertIsDisplayed()

        // Click delete icon button
        composeTestRule.onNodeWithContentDescription("Delete Task").performClick()

        // Verify delete confirmation dialog appears
        composeTestRule.onNodeWithText("Delete Task?").assertIsDisplayed()

        // Confirm deletion
        composeTestRule.onNodeWithText("Delete").performClick()

        // Verify that the task is removed from the UI
        composeTestRule.onNodeWithText("Task to Delete").assertDoesNotExist()
        composeTestRule.onNodeWithText("No tasks found").assertIsDisplayed()
    }

    // 4. Search Task Flow
    @Test
    fun testSearchTaskFlow() {
        // Search for a task and verify that matching tasks appear.
        val allTasks = listOf(
            Todo(id = 1, title = "Team Standup Meeting", completed = false),
            Todo(id = 2, title = "Grocery Shopping", completed = false),
            Todo(id = 3, title = "Write Documentation", completed = false)
        )
        var searchQuery by mutableStateOf("")

        composeTestRule.setContent {
            val filteredTasks = if (searchQuery.isBlank()) {
                allTasks
            } else {
                allTasks.filter { it.title.contains(searchQuery, ignoreCase = true) }
            }
            Column {
                TodoSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
                TaskListContent(
                    tasks = filteredTasks,
                    emptyMessage = "No tasks found",
                    onToggle = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onFocusClick = {},
                    onTodoClick = {}
                )
            }
        }

        // Verify all tasks initially present
        composeTestRule.onNodeWithText("Team Standup Meeting").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grocery Shopping").assertIsDisplayed()
        composeTestRule.onNodeWithText("Write Documentation").assertIsDisplayed()

        // Open search and enter query
        composeTestRule.onNodeWithText("Search tasks").performTextInput("Standup")

        // Verify matching task appears and other tasks are filtered out
        composeTestRule.onNodeWithText("Team Standup Meeting").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grocery Shopping").assertDoesNotExist()
        composeTestRule.onNodeWithText("Write Documentation").assertDoesNotExist()
    }

    // 5. Navigation Flow
    @Test
    fun testBasicNavigationFlow() {
        // Test navigation: Dashboard -> Tasks -> Add Task -> Task Details
        var currentScreen by mutableStateOf(Screen.DASHBOARD)
        var showAddDialog by mutableStateOf(false)
        var selectedTaskForDetails by mutableStateOf<Todo?>(null)

        val sampleTask = Todo(id = 1, title = "Important Project", completed = false)

        composeTestRule.setContent {
            Scaffold(
                bottomBar = {
                    BottomNavBar(selected = currentScreen, onSelect = { currentScreen = it })
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Task")
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (currentScreen) {
                        Screen.DASHBOARD -> {
                            Text("Dashboard View")
                        }
                        Screen.ALL -> {
                            Column {
                                Text("All Tasks View")
                                TodoItemRow(
                                    todo = sampleTask,
                                    onToggle = {},
                                    onEditClick = {},
                                    onDeleteClick = {},
                                    onFocusClick = {},
                                    onTodoClick = {
                                        selectedTaskForDetails = sampleTask
                                        currentScreen = Screen.TASK_DETAILS
                                    }
                                )
                            }
                        }
                        Screen.TASK_DETAILS -> {
                            selectedTaskForDetails?.let {
                                TaskDetailsScreen(
                                    todo = it,
                                    onBack = { currentScreen = Screen.ALL },
                                    isDark = false
                                )
                            }
                        }
                        else -> {}
                    }

                    if (showAddDialog) {
                        AddTaskDialog(
                            title = "",
                            onTitleChange = {},
                            description = "",
                            onDescriptionChange = {},
                            priority = Priority.MEDIUM,
                            onPriorityChange = {},
                            dueTimeMillis = null,
                            onDueTimeChange = {},
                            endTimeMillis = null,
                            onEndTimeChange = {},
                            notificationEnabled = false,
                            onNotificationEnabledChange = {},
                            notificationMinutesBefore = 10,
                            onNotificationMinutesBeforeChange = {},
                            attachmentUri = null,
                            onAttachmentChange = {},
                            onConfirm = { showAddDialog = false },
                            onDismiss = { showAddDialog = false }
                        )
                    }
                }
            }
        }

        // 1. Dashboard is displayed initially
        composeTestRule.onNodeWithText("Dashboard View").assertIsDisplayed()

        // 2. Dashboard -> Tasks
        composeTestRule.onNodeWithText("All").performClick()
        composeTestRule.onNodeWithText("All Tasks View").assertIsDisplayed()

        // 3. Tasks -> Add Task
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        composeTestRule.onNodeWithText("New Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").performClick()

        // 4. Tasks -> Task Details
        composeTestRule.onNodeWithText("Important Project").performClick()
        composeTestRule.onNodeWithText("Task Details").assertIsDisplayed()
    }

    // 6. Focus Timer Flow
    @Test
    fun testFocusTimerFlow() {
        // Open Focus Timer, start the timer, verify running state, and reset.
        val task = Todo(id = 1, title = "Focus Sprint", completed = false)
        var showTimerDialog by mutableStateOf(true)

        composeTestRule.setContent {
            if (showTimerDialog) {
                FocusTimerDialog(
                    todo = task,
                    onDismiss = { showTimerDialog = false },
                    onSessionComplete = {}
                )
            }
        }

        // Verify Focus Timer dialog is displayed
        composeTestRule.onNodeWithText("Focusing on: Focus Sprint").assertIsDisplayed()
        composeTestRule.onNodeWithText("25:00").assertIsDisplayed()

        // Start the timer
        composeTestRule.onNodeWithContentDescription("Start").performClick()

        // Verify that the timer enters the running state
        composeTestRule.onNodeWithContentDescription("Pause").assertIsDisplayed()

        // Reset timer
        composeTestRule.onNodeWithContentDescription("Reset").performClick()
        composeTestRule.onNodeWithContentDescription("Start").assertIsDisplayed()
        composeTestRule.onNodeWithText("25:00").assertIsDisplayed()
    }
}
