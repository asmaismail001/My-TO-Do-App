package com.example.mytodoapp.ui

import android.Manifest
import android.app.AlarmManager
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.net.Uri
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
 
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.components.AddTaskDialog
import com.example.mytodoapp.ui.components.BottomNavBar
import com.example.mytodoapp.ui.components.CalendarView
import com.example.mytodoapp.ui.components.DashboardScreen
import com.example.mytodoapp.ui.components.DeleteTaskDialog
import com.example.mytodoapp.ui.components.EditTaskDialog
import com.example.mytodoapp.ui.components.ConflictDialog
import com.example.mytodoapp.ui.components.SettingsDrawerContent
import com.example.mytodoapp.ui.components.CustomDateTimePickerDialog
import com.example.mytodoapp.ui.components.TaskListContent
import com.example.mytodoapp.ui.components.TaskDetailsScreen
import com.example.mytodoapp.ui.components.TodoSearchBar
import com.example.mytodoapp.util.CalendarUtil
import com.example.mytodoapp.util.DateTimePickerUtil
import com.example.mytodoapp.util.PreferencesManager
import com.example.mytodoapp.viewmodel.SwapTimeResult
import com.example.mytodoapp.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    authViewModel: com.example.mytodoapp.viewmodel.AuthViewModel,
    profileViewModel: com.example.mytodoapp.viewmodel.ProfileViewModel,
    openTaskId: Int? = null,
    onOpenTaskConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var themeMode by remember { mutableStateOf(prefs.getThemeMode()) }
    val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = remember(themeMode, systemInDark) {
        when (themeMode) {
            "light" -> false
            "dark" -> true
            else -> systemInDark
        }
    }
    var notificationsEnabled by remember { mutableStateOf(prefs.areNotificationsEnabled()) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> viewModel.exportTasks(context, uri) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> viewModel.importTasks(context, uri) }

    var currentScreen by remember {
        mutableStateOf(if (authViewModel.isLoggedIn) Screen.DASHBOARD else Screen.LOGIN)
    }
    var previousScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var selectedTodoForDetails by remember { mutableStateOf<Todo?>(null) }

    val navigateToDetails: (Todo) -> Unit = { todo ->
        previousScreen = currentScreen
        selectedTodoForDetails = todo
        currentScreen = Screen.TASK_DETAILS
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var newDueTime by remember { mutableStateOf<Long?>(null) }
    var newEndTime by remember { mutableStateOf<Long?>(null) }
    var newNotificationEnabled by remember { mutableStateOf(false) }
    var newNotificationMinutesBefore by remember { mutableStateOf(10) }
    var newAttachmentUri by remember { mutableStateOf<String?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<Todo?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var editDueTime by remember { mutableStateOf<Long?>(null) }
    var editEndTime by remember { mutableStateOf<Long?>(null) }
    var editNotificationEnabled by remember { mutableStateOf(false) }
    var editNotificationMinutesBefore by remember { mutableStateOf(10) }
    var editAttachmentUri by remember { mutableStateOf<String?>(null) }

    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictingTodo by remember { mutableStateOf<Todo?>(null) }
    var conflictResolutionCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingTodo by remember { mutableStateOf<Todo?>(null) }

    var focusTimerTodo by remember { mutableStateOf<Todo?>(null) }

    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    var validationErrorTitle by remember { mutableStateOf("Invalid Task Time") }

    var showExactAlarmSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val hasExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (!hasExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                showExactAlarmSettingsDialog = true
            } else {
                if (showAddDialog) newNotificationEnabled = true
                if (showEditDialog) editNotificationEnabled = true
            }
        } else {
            validationErrorTitle = "Permission Required"
            validationErrorMessage = "Notification permission is required to send reminders for your tasks."
            showValidationErrorDialog = true
            if (showAddDialog) {
                newNotificationEnabled = false
            }
            if (showEditDialog) {
                editNotificationEnabled = false
            }
        }
    }

    var showRescheduleConfirm by remember { mutableStateOf(false) }
    var rescheduleNewStart by remember { mutableStateOf(0L) }
    var rescheduleNewEnd by remember { mutableStateOf(0L) }

    var showReschedulePicker by remember { mutableStateOf(false) }
    var reschedulePickerIsStart by remember { mutableStateOf(true) }
    var rescheduleTempStart by remember { mutableStateOf<Long?>(null) }

    var selectedDay by remember { mutableStateOf(Calendar.getInstance()) }
    var visibleMonth by remember { mutableStateOf(Calendar.getInstance()) }

    fun openEdit(todo: Todo) {
        editingTodo = todo
        editTitle = todo.title
        editDescription = todo.description
        editPriority = todo.priority
        editDueTime = todo.dueTimeMillis
        editEndTime = todo.endTimeMillis
        editNotificationEnabled = todo.notificationEnabled
        editNotificationMinutesBefore = todo.notificationMinutesBefore
        editAttachmentUri = todo.attachmentUri
        showEditDialog = true
    }

    fun openDelete(todo: Todo) {
        deletingTodo = todo
        showDeleteDialog = true
    }

    if (currentScreen == Screen.TASK_DETAILS) {
        BackHandler {
            currentScreen = previousScreen
            selectedTodoForDetails = null
        }
    }

    val refreshReceiver = remember {
        object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                viewModel.loadTodos()
            }
        }
    }
    DisposableEffect(Unit) {
        val filter = IntentFilter("com.example.mytodoapp.REFRESH_TODOS")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(refreshReceiver, filter)
        }
        onDispose {
            context.unregisterReceiver(refreshReceiver)
        }
    }

    LaunchedEffect(openTaskId, viewModel.todoList) {
        val id = openTaskId ?: return@LaunchedEffect
        if (id <= 0) return@LaunchedEffect
        val todo = viewModel.todoList.find { it.id == id } ?: return@LaunchedEffect
        previousScreen = if (currentScreen == Screen.TASK_DETAILS) previousScreen else currentScreen
        selectedTodoForDetails = todo
        currentScreen = Screen.TASK_DETAILS
        onOpenTaskConsumed()
    }

    LaunchedEffect(viewModel.todoList) {
        val current = selectedTodoForDetails ?: return@LaunchedEffect
        selectedTodoForDetails = viewModel.todoList.find { it.id == current.id } ?: current
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = surfaceColorFor(isDarkTheme),
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDarkTheme)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out?",
                    color = textSecondaryFor(isDarkTheme)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        profileViewModel.logout {
                            authViewModel.resetState()
                            viewModel.loadTodos()
                            currentScreen = Screen.LOGIN
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed, contentColor = Color.White)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = textSecondaryFor(isDarkTheme))
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val showMainBars = currentScreen in listOf(
        Screen.DASHBOARD, Screen.ALL, Screen.COMPLETED, Screen.PENDING, Screen.CALENDAR
    )

    CompositionLocalProvider(LocalIsDarkTheme provides isDarkTheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SettingsDrawerContent(
                    currentScreen = currentScreen,
                    onScreenSelect = { screen ->
                        currentScreen = screen
                        scope.launch { drawerState.close() }
                    },
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        prefs.setThemeMode(mode)
                    },
                    isDarkTheme = isDarkTheme,
                    notificationsEnabled = notificationsEnabled,
                    onNotificationsChange = {
                        notificationsEnabled = it
                        prefs.setNotificationsEnabled(it)
                        viewModel.onGlobalNotificationsChanged(it)
                    },
                    onExportClick = { exportLauncher.launch("todo_backup.json") },
                    onImportClick = { importLauncher.launch(arrayOf("application/json")) },
                    onLogoutClick = {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    }
                )
            }
        ) {
            Scaffold(
                containerColor = backgroundColorFor(isDarkTheme),
                topBar = {
                    if (showMainBars) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (currentScreen == Screen.DASHBOARD) "Dashboard" else "Task Manager",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Settings", tint = textPrimaryFor(isDarkTheme))
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = backgroundColorFor(isDarkTheme),
                                titleContentColor = textPrimaryFor(isDarkTheme)
                            )
                        )
                    }
                },
                bottomBar = {
                    if (showMainBars) {
                        BottomNavBar(selected = currentScreen, onSelect = { currentScreen = it })
                    }
                },
                floatingActionButton = {
                    if (showMainBars) {
                        FloatingActionButton(
                            onClick = { showAddDialog = true },
                            containerColor = Accent,
                            contentColor = Color.White,
                            shape = androidx.compose.foundation.shape.CircleShape
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Task")
                        }
                    }
                }
            ) { paddingValues ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (showMainBars) paddingValues else PaddingValues(0.dp))
                ) {
                    when (currentScreen) {
                        Screen.LOGIN -> {
                            com.example.mytodoapp.ui.auth.LoginScreen(
                                viewModel = authViewModel,
                                onNavigateToSignup = { currentScreen = Screen.SIGNUP },
                                onLoginSuccess = {
                                    viewModel.loadTodos()
                                    currentScreen = Screen.DASHBOARD
                                }
                            )
                        }

                        Screen.SIGNUP -> {
                            com.example.mytodoapp.ui.auth.SignupScreen(
                                viewModel = authViewModel,
                                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                                onSignupSuccess = {
                                    viewModel.loadTodos()
                                    currentScreen = Screen.DASHBOARD
                                }
                            )
                        }

                        Screen.PROFILE -> {
                            com.example.mytodoapp.ui.profile.ProfileScreen(
                                viewModel = profileViewModel,
                                onBack = { currentScreen = Screen.DASHBOARD },
                                onNavigateToEditProfile = { currentScreen = Screen.EDIT_PROFILE },
                                onLogoutSuccess = {
                                    authViewModel.resetState()
                                    viewModel.loadTodos()
                                    currentScreen = Screen.LOGIN
                                }
                            )
                        }

                        Screen.EDIT_PROFILE -> {
                            com.example.mytodoapp.ui.profile.EditProfileScreen(
                                viewModel = profileViewModel,
                                onBack = { currentScreen = Screen.PROFILE }
                            )
                        }

                        Screen.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                profileViewModel = profileViewModel,
                                onToggle = { viewModel.toggleTodo(it) },
                                onEditClick = { openEdit(it) },
                                onDeleteClick = { openDelete(it) },
                                onAddTaskClick = { showAddDialog = true },
                                onFocusOpen = { focusTimerTodo = it },
                                onTodoClick = navigateToDetails,
                                onProfileClick = { currentScreen = Screen.PROFILE }
                            )
                        }

                        Screen.ALL -> {
                            TodoSearchBar(
                                query = viewModel.searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChange(it) }
                            )
                            val list = viewModel.allTasks
                            val completedCount = viewModel.todoList.count { it.completed }
                            val total = viewModel.todoList.size

                            val progress = if (total > 0) completedCount.toFloat() / total.toFloat() else 0f
                            val percentage = (progress * 100).toInt()
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDarkTheme)),
                                border = BorderStroke(1.dp, cardBorderColorFor(isDarkTheme))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Today's Progress",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = textPrimaryFor(isDarkTheme)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (total == 0) "No tasks for today" else "$completedCount of $total tasks completed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecondaryFor(isDarkTheme)
                                        )
                                        if (total > 0) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = Accent,
                                                trackColor = if (isDarkTheme) Color(0xFF222836) else Color(0xFFEEF0F3)
                                            )
                                        }
                                    }

                                    if (total > 0) {
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Accent.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "$percentage%",
                                                color = Accent,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }
                                }
                            }

                            TaskListContent(
                                tasks = list,
                                emptyMessage = "No tasks yet. Tap + to add one.",
                                onToggle = { viewModel.toggleTodo(it) },
                                onEditClick = { openEdit(it) },
                                onDeleteClick = { openDelete(it) },
                                onFocusClick = { focusTimerTodo = it },
                                onTodoClick = navigateToDetails
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
                                onDeleteClick = { openDelete(it) },
                                onFocusClick = { focusTimerTodo = it },
                                onTodoClick = navigateToDetails
                            )
                        }

                        Screen.PENDING -> {
                            TodoSearchBar(
                                query = viewModel.searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChange(it) }
                            )
                            TaskListContent(
                                tasks = viewModel.pendingTasks,
                                emptyMessage = "No pending tasks. You're all caught up!",
                                onToggle = { viewModel.toggleTodo(it) },
                                onEditClick = { openEdit(it) },
                                onDeleteClick = { openDelete(it) },
                                onFocusClick = { focusTimerTodo = it },
                                onTodoClick = navigateToDetails
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
                                onDeleteClick = { openDelete(it) },
                                onFocusClick = { focusTimerTodo = it },
                                onTodoClick = navigateToDetails
                            )
                        }

                        Screen.TASK_DETAILS -> {
                            selectedTodoForDetails?.let { todo ->
                                TaskDetailsScreen(
                                    todo = todo,
                                    onBack = {
                                        currentScreen = previousScreen
                                        selectedTodoForDetails = null
                                    },
                                    isDark = isDarkTheme,
                                    eligibleSwapTasks = viewModel.eligibleSwapTasks(todo),
                                    onConfirmSwap = { other ->
                                        viewModel.swapTimeSlots(todo, other) { result ->
                                            if (result == SwapTimeResult.CONFLICT || result == SwapTimeResult.INVALID) {
                                                validationErrorTitle = "Time swap unavailable"
                                                validationErrorMessage =
                                                    "One of the new time slots is already occupied by another task."
                                                showValidationErrorDialog = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val clearAddStates = {
        newTitle = ""
        newDescription = ""
        newPriority = Priority.MEDIUM
        newDueTime = null
        newEndTime = null
        newNotificationEnabled = false
        newNotificationMinutesBefore = 10
        newAttachmentUri = null
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
            endTimeMillis = newEndTime,
            onEndTimeChange = { newEndTime = it },
            notificationEnabled = newNotificationEnabled,
            onNotificationEnabledChange = { enabled ->
                if (enabled) {
                    val hasPostNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }

                    val hasExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.canScheduleExactAlarms()
                    } else {
                        true
                    }

                    if (!hasPostNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (!hasExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        showExactAlarmSettingsDialog = true
                    } else {
                        newNotificationEnabled = true
                    }
                } else {
                    newNotificationEnabled = false
                }
            },
            notificationMinutesBefore = newNotificationMinutesBefore,
            onNotificationMinutesBeforeChange = { newNotificationMinutesBefore = it },
            attachmentUri = newAttachmentUri,
            onAttachmentChange = { newAttachmentUri = it },
            onConfirm = {
                if (newNotificationEnabled && newDueTime == null) {
                    validationErrorTitle = "Start time required"
                    validationErrorMessage = "Set a start date and time so the reminder can fire before the task starts."
                    showValidationErrorDialog = true
                } else if (newDueTime != null && newEndTime != null && newEndTime!! <= newDueTime!!) {
                    validationErrorTitle = "Invalid Task Time"
                    validationErrorMessage = "Due time must be later than the start time."
                    showValidationErrorDialog = true
                } else {
                    val conflict = viewModel.checkTimeConflict(newDueTime, newEndTime)
                    if (conflict != null) {
                        conflictingTodo = conflict
                        conflictResolutionCallback = {
                            viewModel.addTodo(
                                newTitle,
                                newDescription,
                                newPriority,
                                newDueTime,
                                newEndTime,
                                newAttachmentUri,
                                newNotificationEnabled,
                                newNotificationMinutesBefore
                            )
                            clearAddStates()
                            showAddDialog = false
                        }
                        showConflictDialog = true
                    } else {
                        viewModel.addTodo(
                            newTitle,
                            newDescription,
                            newPriority,
                            newDueTime,
                            newEndTime,
                            newAttachmentUri,
                            newNotificationEnabled,
                            newNotificationMinutesBefore
                        )
                        clearAddStates()
                        showAddDialog = false
                    }
                }
            },
            onDismiss = {
                showAddDialog = false
                clearAddStates()
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
            endTimeMillis = editEndTime,
            onEndTimeChange = { editEndTime = it },
            notificationEnabled = editNotificationEnabled,
            onNotificationEnabledChange = { enabled ->
                if (enabled) {
                    val hasPostNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }

                    val hasExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.canScheduleExactAlarms()
                    } else {
                        true
                    }

                    if (!hasPostNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (!hasExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        showExactAlarmSettingsDialog = true
                    } else {
                        editNotificationEnabled = true
                    }
                } else {
                    editNotificationEnabled = false
                }
            },
            notificationMinutesBefore = editNotificationMinutesBefore,
            onNotificationMinutesBeforeChange = { editNotificationMinutesBefore = it },
            attachmentUri = editAttachmentUri,
            onAttachmentChange = { editAttachmentUri = it },
            createdAt = editingTodo?.createdAt ?: System.currentTimeMillis(),
            onConfirm = {
                if (editNotificationEnabled && editDueTime == null) {
                    validationErrorTitle = "Start time required"
                    validationErrorMessage = "Set a start date and time so the reminder can fire before the task starts."
                    showValidationErrorDialog = true
                } else if (editDueTime != null && editEndTime != null && editEndTime!! <= editDueTime!!) {
                    validationErrorTitle = "Invalid Task Time"
                    validationErrorMessage = "Due time must be later than the start time."
                    showValidationErrorDialog = true
                } else {
                    val conflict = viewModel.checkTimeConflict(editDueTime, editEndTime, editingTodo?.id ?: 0)
                    if (conflict != null) {
                        conflictingTodo = conflict
                        conflictResolutionCallback = {
                            editingTodo?.let {
                                viewModel.updateTodo(
                                    it,
                                    editTitle,
                                    editDescription,
                                    editPriority,
                                    editDueTime,
                                    editEndTime,
                                    editAttachmentUri,
                                    editNotificationEnabled,
                                    editNotificationMinutesBefore
                                )
                            }
                            showEditDialog = false
                            editingTodo = null
                        }
                        showConflictDialog = true
                    } else {
                        editingTodo?.let {
                            viewModel.updateTodo(
                                it,
                                editTitle,
                                editDescription,
                                editPriority,
                                editDueTime,
                                editEndTime,
                                editAttachmentUri,
                                editNotificationEnabled,
                                editNotificationMinutesBefore
                            )
                        }
                        showEditDialog = false
                        editingTodo = null
                    }
                }
            },
            onDismiss = {
                showEditDialog = false
                editingTodo = null
            }
        )
    }

    if (showConflictDialog && conflictingTodo != null) {
        ConflictDialog(
            conflictingTodo = conflictingTodo!!,
            onDismiss = {
                showConflictDialog = false
                conflictingTodo = null
                conflictResolutionCallback = null
            },
            onViewConflictingTask = {
                val targetTodo = conflictingTodo!!
                showConflictDialog = false
                conflictingTodo = null
                conflictResolutionCallback = null
                showAddDialog = false
                showEditDialog = false
                editingTodo = null
                navigateToDetails(targetTodo)
            },
            onRescheduleConflictingTask = {
                val initialStart = conflictingTodo!!.dueTimeMillis
                rescheduleTempStart = initialStart
                reschedulePickerIsStart = true
                showReschedulePicker = true
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

    focusTimerTodo?.let { todo ->
        FocusTimerDialog(
            todo = todo,
            onDismiss = { focusTimerTodo = null },
            onSessionComplete = { /* optional: show a snackbar or increment a stat */ }
        )
    }

    if (showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showValidationErrorDialog = false },
            title = {
                Text(
                    text = validationErrorTitle,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDarkTheme)
                )
            },
            text = {
                Text(
                    text = validationErrorMessage,
                    color = textSecondaryFor(isDarkTheme)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showValidationErrorDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = surfaceColorFor(isDarkTheme)
        )
    }

    if (showExactAlarmSettingsDialog) {
        AlertDialog(
            onDismissRequest = {
                showExactAlarmSettingsDialog = false
                if (showAddDialog) newNotificationEnabled = false
                if (showEditDialog) editNotificationEnabled = false
            },
            title = {
                Text(
                    text = "Exact Reminders Permission",
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDarkTheme)
                )
            },
            text = {
                Text(
                    text = "To trigger reminders exactly on time, the app needs the \"Alarms & Reminders\" permission. Please enable it in the system settings page.",
                    color = textSecondaryFor(isDarkTheme)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExactAlarmSettingsDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open settings. Please enable exact alarms manually.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExactAlarmSettingsDialog = false
                        if (showAddDialog) newNotificationEnabled = true
                        if (showEditDialog) editNotificationEnabled = true
                    }
                ) {
                    Text("Use Non-Exact", color = textSecondaryFor(isDarkTheme))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = surfaceColorFor(isDarkTheme)
        )
    }

    if (showRescheduleConfirm && conflictingTodo != null) {
        AlertDialog(
            onDismissRequest = { showRescheduleConfirm = false },
            title = {
                Text(
                    text = "Confirm Reschedule",
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDarkTheme)
                )
            },
            text = {
                Text(
                    text = "Do you want to reschedule \"${conflictingTodo!!.title}\" to:\n${DateTimePickerUtil.formatTimeRange(rescheduleNewStart, rescheduleNewEnd)}?",
                    color = textSecondaryFor(isDarkTheme)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newConflict = viewModel.checkTimeConflict(rescheduleNewStart, rescheduleNewEnd, excludeTaskId = conflictingTodo!!.id)
                        if (newConflict != null) {
                            conflictingTodo = newConflict
                            Toast.makeText(context, "The new slot is also occupied! Please choose another time.", Toast.LENGTH_LONG).show()
                        } else {
                            val currentConf = conflictingTodo!!
                            viewModel.updateTodo(
                                todo = currentConf,
                                newTitle = currentConf.title,
                                newDescription = currentConf.description,
                                newPriority = currentConf.priority,
                                newDueTimeMillis = rescheduleNewStart,
                                newEndTimeMillis = rescheduleNewEnd,
                                newAttachmentUri = currentConf.attachmentUri,
                                newNotificationEnabled = currentConf.notificationEnabled,
                                newNotificationMinutesBefore = currentConf.notificationMinutesBefore
                            )
                            conflictResolutionCallback?.invoke()
                            showConflictDialog = false
                            conflictingTodo = null
                            conflictResolutionCallback = null
                        }
                        showRescheduleConfirm = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleConfirm = false }) {
                    Text("Cancel", color = textSecondaryFor(isDarkTheme))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = surfaceColorFor(isDarkTheme)
        )
    }

    if (showReschedulePicker) {
        val pickerInitialTime = if (reschedulePickerIsStart) {
            rescheduleTempStart
        } else {
            conflictingTodo!!.endTimeMillis ?: (rescheduleTempStart!! + 3600000L)
        }
        CustomDateTimePickerDialog(
            initialTime = pickerInitialTime,
            onDismiss = { showReschedulePicker = false },
            onSave = { picked ->
                if (reschedulePickerIsStart) {
                    rescheduleTempStart = picked
                    reschedulePickerIsStart = false
                } else {
                    showReschedulePicker = false
                    val start = rescheduleTempStart!!
                    if (picked <= start) {
                        validationErrorMessage = "Due time must be later than the start time."
                        showValidationErrorDialog = true
                    } else {
                        rescheduleNewStart = start
                        rescheduleNewEnd = picked
                        showRescheduleConfirm = true
                    }
                }
            }
        )
    }
}