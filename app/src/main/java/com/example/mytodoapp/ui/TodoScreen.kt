package com.example.mytodoapp.ui

import android.Manifest
import android.app.AlarmManager
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mytodoapp.util.NotificationPermissionHelper
import android.net.Uri
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
 
import com.example.mytodoapp.R
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.TaskType
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.model.WeatherUiState
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
import com.example.mytodoapp.ui.settings.SettingsScreen
import com.example.mytodoapp.util.CalendarUtil
import com.example.mytodoapp.util.DateTimePickerUtil
import com.example.mytodoapp.util.LocationHelper
import com.example.mytodoapp.util.PreferencesManager
import com.example.mytodoapp.viewmodel.SwapTimeResult
import com.example.mytodoapp.viewmodel.TodoViewModel
import com.example.mytodoapp.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    authViewModel: com.example.mytodoapp.viewmodel.AuthViewModel,
    profileViewModel: com.example.mytodoapp.viewmodel.ProfileViewModel,
    weatherViewModel: WeatherViewModel? = null,
    openTaskId: Int? = null,
    onOpenTaskConsumed: () -> Unit = {},
    themeMode: String = "system",
    onThemeModeChange: (String) -> Unit = {},
    currentLanguage: String = "en",
    onLanguageChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted && weatherViewModel != null) {
            weatherViewModel.loadCurrentWeather(context, forceRefresh = true)
        }
    }

    LaunchedEffect(Unit) {
        if (!LocationHelper.hasLocationPermission(context) && !prefs.hasRequestedLocationPermission()) {
            prefs.setLocationPermissionRequested(true)
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val isDarkTheme = LocalIsDarkTheme.current
    val sessionManager = remember { com.example.mytodoapp.util.SessionManager(context) }
    var biometricLockEnabled by remember { mutableStateOf(sessionManager.isBiometricLockEnabled()) }
    var notificationsEnabled by remember { mutableStateOf(prefs.areNotificationsEnabled()) }
    var defaultReminderMinutes by remember { mutableIntStateOf(prefs.getDefaultReminderMinutes()) }
    var lastBackupTime by remember { mutableStateOf(prefs.getLastBackupTime()) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        viewModel.exportTasks(
            context = context,
            uri = uri,
            onSuccess = {
                lastBackupTime = prefs.getLastBackupTime()
                Toast.makeText(context, context.getString(R.string.export_success), Toast.LENGTH_SHORT).show()
            },
            onError = { msg ->
                Toast.makeText(context, "${context.getString(R.string.export_error)}: $msg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        viewModel.importTasks(
            context = context,
            uri = uri,
            onSuccess = { count ->
                lastBackupTime = prefs.getLastBackupTime()
                Toast.makeText(context, context.getString(R.string.import_success, count), Toast.LENGTH_SHORT).show()
            },
            onError = {
                Toast.makeText(context, context.getString(R.string.import_invalid_data), Toast.LENGTH_SHORT).show()
            }
        )
    }

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
    var newTaskType by remember { mutableStateOf(TaskType.FLEXIBLE) }
    var newDueTime by remember { mutableStateOf<Long?>(null) }
    var newEndTime by remember { mutableStateOf<Long?>(null) }
    var newNotificationEnabled by remember { mutableStateOf(false) }
    var newNotificationMinutesBefore by remember { mutableIntStateOf(defaultReminderMinutes) }
    var newAttachmentUri by remember { mutableStateOf<String?>(null) }
    var newTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var newRecurrence by remember { mutableStateOf(com.example.mytodoapp.model.RecurrenceType.NONE) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<Todo?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var editTaskType by remember { mutableStateOf(TaskType.FLEXIBLE) }
    var editDueTime by remember { mutableStateOf<Long?>(null) }
    var editEndTime by remember { mutableStateOf<Long?>(null) }
    var editNotificationEnabled by remember { mutableStateOf(false) }
    var editNotificationMinutesBefore by remember { mutableIntStateOf(10) }
    var editAttachmentUri by remember { mutableStateOf<String?>(null) }
    var editTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var editRecurrence by remember { mutableStateOf(com.example.mytodoapp.model.RecurrenceType.NONE) }

    var showReschedulePickerForDetails by remember { mutableStateOf(false) }

    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictingTodo by remember { mutableStateOf<Todo?>(null) }
    var conflictResolutionCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingTodo by remember { mutableStateOf<Todo?>(null) }

    var focusTimerTodo by remember { mutableStateOf<Todo?>(null) }

    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    var validationErrorTitle by remember { mutableStateOf("") }

    var showExactAlarmSettingsDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        prefs.setNotificationPermissionRequested(true)
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
            if (showAddDialog) {
                newNotificationEnabled = false
            }
            if (showEditDialog) {
                editNotificationEnabled = false
            }
            Toast.makeText(context, context.getString(R.string.notifications_disabled_msg), Toast.LENGTH_SHORT).show()
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
        editTaskType = todo.taskType
        editDueTime = todo.dueTimeMillis
        editEndTime = todo.endTimeMillis
        editNotificationEnabled = todo.notificationEnabled
        editNotificationMinutesBefore = todo.notificationMinutesBefore
        editAttachmentUri = todo.attachmentUri
        editTags = todo.tags
        editRecurrence = todo.recurrence
        weatherViewModel?.loadTaskWeather(todo.dueTimeMillis ?: todo.createdAt, todo.taskType)
        showEditDialog = true
    }

    fun openDelete(todo: Todo) {
        deletingTodo = todo
        showDeleteDialog = true
    }

    if (currentScreen in listOf(Screen.TASK_DETAILS, Screen.SETTINGS, Screen.PROFILE, Screen.EDIT_PROFILE)) {
        BackHandler {
            when (currentScreen) {
                Screen.EDIT_PROFILE -> currentScreen = Screen.PROFILE
                Screen.PROFILE -> currentScreen = previousScreen
                Screen.SETTINGS -> currentScreen = previousScreen
                Screen.TASK_DETAILS -> {
                    currentScreen = previousScreen
                    selectedTodoForDetails = null
                }
                else -> {}
            }
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
                    text = stringResource(R.string.logout_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDarkTheme)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_confirm_msg),
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
                    Text(stringResource(R.string.logout))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = textSecondaryFor(isDarkTheme))
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val showMainBars = currentScreen in listOf(
        Screen.DASHBOARD, Screen.ALL, Screen.COMPLETED, Screen.PENDING, Screen.CALENDAR
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawerContent(
                currentScreen = currentScreen,
                onScreenSelect = { screen ->
                    if (screen == Screen.SETTINGS || screen == Screen.PROFILE) {
                        previousScreen = if (currentScreen in listOf(Screen.SETTINGS, Screen.PROFILE, Screen.EDIT_PROFILE, Screen.TASK_DETAILS)) previousScreen else currentScreen
                    }
                    currentScreen = screen
                    scope.launch { drawerState.close() }
                },
                isDarkTheme = isDarkTheme,
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
                                text = if (currentScreen == Screen.DASHBOARD) stringResource(R.string.dashboard) else stringResource(R.string.app_name),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu), tint = textPrimaryFor(isDarkTheme))
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                previousScreen = if (currentScreen in listOf(Screen.SETTINGS, Screen.PROFILE, Screen.EDIT_PROFILE, Screen.TASK_DETAILS)) previousScreen else currentScreen
                                currentScreen = Screen.SETTINGS
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = stringResource(R.string.settings),
                                    tint = textPrimaryFor(isDarkTheme)
                                )
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
                    val fabInteractionSource = remember { MutableInteractionSource() }
                    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
                    val fabScale by animateFloatAsState(
                        targetValue = if (isFabPressed) 0.92f else 1.0f,
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                        label = "fabScale"
                    )

                    FloatingActionButton(
                        onClick = {
                            newNotificationMinutesBefore = defaultReminderMinutes
                            showAddDialog = true
                        },
                        interactionSource = fabInteractionSource,
                        containerColor = Accent,
                        contentColor = Color.White,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        modifier = Modifier.graphicsLayer {
                            scaleX = fabScale
                            scaleY = fabScale
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task))
                    }
                }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (showMainBars) paddingValues else PaddingValues(0.dp))
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    label = "screenTransition",
                    transitionSpec = {
                        fadeIn(tween(220, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(tween(180, easing = FastOutSlowInEasing))
                    }
                ) { screen ->
                    when (screen) {
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
                            onBack = { currentScreen = previousScreen },
                            onNavigateToEditProfile = { currentScreen = Screen.EDIT_PROFILE },
                            onNavigateToSettings = {
                                previousScreen = currentScreen
                                currentScreen = Screen.SETTINGS
                            },
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

                    Screen.SETTINGS -> {
                        SettingsScreen(
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            biometricLockEnabled = biometricLockEnabled,
                            onBiometricLockEnabledChange = { enabled ->
                                biometricLockEnabled = enabled
                                sessionManager.setBiometricLockEnabled(enabled)
                            },
                            notificationsEnabled = notificationsEnabled,
                            onNotificationsEnabledChange = { enabled ->
                                notificationsEnabled = enabled
                                prefs.setNotificationsEnabled(enabled)
                                viewModel.onGlobalNotificationsChanged(enabled)
                            },
                            defaultReminderMinutes = defaultReminderMinutes,
                            onDefaultReminderMinutesChange = { minutes ->
                                defaultReminderMinutes = minutes
                                prefs.setDefaultReminderMinutes(minutes)
                            },
                            currentLanguage = currentLanguage,
                            onLanguageChange = onLanguageChange,
                            lastBackupTime = lastBackupTime,
                            onExportClick = { exportLauncher.launch("todo_backup.json") },
                            onImportClick = { importLauncher.launch(arrayOf("application/json")) },
                            onBack = { currentScreen = previousScreen }
                        )
                    }

                    Screen.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            profileViewModel = profileViewModel,
                            weatherViewModel = weatherViewModel,
                            onToggle = { viewModel.toggleTodo(it) },
                            onEditClick = { openEdit(it) },
                            onDeleteClick = { openDelete(it) },
                            onAddTaskClick = {
                                newNotificationMinutesBefore = defaultReminderMinutes
                                showAddDialog = true
                            },
                            onFocusOpen = { focusTimerTodo = it },
                            onTodoClick = navigateToDetails,
                            onProfileClick = {
                                previousScreen = currentScreen
                                currentScreen = Screen.PROFILE
                            }
                        )
                    }

                    Screen.ALL -> {
                        TodoSearchBar(
                            query = viewModel.searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            selectedPriority = viewModel.selectedPriorityFilter,
                            onPrioritySelect = { viewModel.onPriorityFilterChange(it) },
                            selectedTag = viewModel.selectedTagFilter,
                            onTagSelect = { viewModel.onTagFilterChange(it) },
                            availableTags = viewModel.allUniqueTags,
                            onClearFilters = { viewModel.clearFilters() }
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
                                        text = stringResource(R.string.task_overview),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = textPrimaryFor(isDarkTheme)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (total == 0) stringResource(R.string.no_tasks_today) else "$completedCount / $total",
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

                        val isFiltered = viewModel.searchQuery.isNotBlank() || viewModel.selectedPriorityFilter != null || viewModel.selectedTagFilter != null
                        TaskListContent(
                            tasks = list,
                            emptyMessage = if (isFiltered) stringResource(R.string.no_tasks_match_search) else stringResource(R.string.no_tasks_found),
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
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            selectedPriority = viewModel.selectedPriorityFilter,
                            onPrioritySelect = { viewModel.onPriorityFilterChange(it) },
                            selectedTag = viewModel.selectedTagFilter,
                            onTagSelect = { viewModel.onTagFilterChange(it) },
                            availableTags = viewModel.allUniqueTags,
                            onClearFilters = { viewModel.clearFilters() }
                        )
                        val isFiltered = viewModel.searchQuery.isNotBlank() || viewModel.selectedPriorityFilter != null || viewModel.selectedTagFilter != null
                        TaskListContent(
                            tasks = viewModel.completedTasks,
                            emptyMessage = if (isFiltered) stringResource(R.string.no_tasks_match_search) else stringResource(R.string.no_completed_tasks),
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
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            selectedPriority = viewModel.selectedPriorityFilter,
                            onPrioritySelect = { viewModel.onPriorityFilterChange(it) },
                            selectedTag = viewModel.selectedTagFilter,
                            onTagSelect = { viewModel.onTagFilterChange(it) },
                            availableTags = viewModel.allUniqueTags,
                            onClearFilters = { viewModel.clearFilters() }
                        )
                        val isFiltered = viewModel.searchQuery.isNotBlank() || viewModel.selectedPriorityFilter != null || viewModel.selectedTagFilter != null
                        TaskListContent(
                            tasks = viewModel.pendingTasks,
                            emptyMessage = if (isFiltered) stringResource(R.string.no_tasks_match_search) else stringResource(R.string.no_pending_tasks),
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
                            (it.dueTimeMillis != null && CalendarUtil.isSameDay(it.dueTimeMillis, selectedDay.timeInMillis)) ||
                            (it.recurrence == com.example.mytodoapp.model.RecurrenceType.DAILY)
                        }

                        TaskListContent(
                            tasks = tasksForSelectedDay,
                            emptyMessage = stringResource(R.string.no_tasks_today),
                            onToggle = { viewModel.toggleTodo(it) },
                            onEditClick = { openEdit(it) },
                            onDeleteClick = { openDelete(it) },
                            onFocusClick = { focusTimerTodo = it },
                            onTodoClick = navigateToDetails
                        )
                    }

                    Screen.TASK_DETAILS -> {
                        selectedTodoForDetails?.let { todo ->
                            LaunchedEffect(todo.id, todo.dueTimeMillis, todo.taskType) {
                                weatherViewModel?.loadTaskWeather(todo.dueTimeMillis ?: todo.createdAt, todo.taskType)
                            }

                            TaskDetailsScreen(
                                todo = todo,
                                onBack = {
                                    currentScreen = previousScreen
                                    selectedTodoForDetails = null
                                },
                                isDark = isDarkTheme,
                                weatherUiState = weatherViewModel?.taskWeatherState ?: WeatherUiState.Idle,
                                onRefreshWeather = {
                                    weatherViewModel?.loadTaskWeather(todo.dueTimeMillis ?: todo.createdAt, todo.taskType)
                                },
                                onRescheduleClick = {
                                    showReschedulePickerForDetails = true
                                },
                                onMarkAsIndoorClick = {
                                    viewModel.updateTodo(
                                        todo = todo,
                                        newTitle = todo.title,
                                        newDescription = todo.description,
                                        newPriority = todo.priority,
                                        newDueTimeMillis = todo.dueTimeMillis,
                                        newEndTimeMillis = todo.endTimeMillis,
                                        newAttachmentUri = todo.attachmentUri,
                                        newNotificationEnabled = todo.notificationEnabled,
                                        newNotificationMinutesBefore = todo.notificationMinutesBefore,
                                        newTaskType = TaskType.INDOOR
                                    )
                                    weatherViewModel?.loadTaskWeather(todo.dueTimeMillis ?: todo.createdAt, TaskType.INDOOR)
                                },
                                eligibleSwapTasks = viewModel.eligibleSwapTasks(todo),
                                onConfirmSwap = { other ->
                                    viewModel.swapTimeSlots(todo, other) { result ->
                                        if (result == SwapTimeResult.CONFLICT || result == SwapTimeResult.INVALID) {
                                            validationErrorTitle = context.getString(R.string.time_slot_unavailable)
                                            validationErrorMessage = context.getString(R.string.time_slot_occupied_msg)
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
        newTaskType = TaskType.FLEXIBLE
        newDueTime = null
        newEndTime = null
        newNotificationEnabled = false
        newNotificationMinutesBefore = defaultReminderMinutes
        newAttachmentUri = null
        newTags = emptyList()
        newRecurrence = com.example.mytodoapp.model.RecurrenceType.NONE
        weatherViewModel?.clearTaskWeather()
    }

    if (showAddDialog) {
        AddTaskDialog(
            title = newTitle,
            onTitleChange = { newTitle = it },
            description = newDescription,
            onDescriptionChange = { newDescription = it },
            priority = newPriority,
            onPriorityChange = { newPriority = it },
            taskType = newTaskType,
            onTaskTypeChange = {
                newTaskType = it
                if (newDueTime != null) {
                    weatherViewModel?.loadTaskWeather(newDueTime, it)
                }
            },
            dueTimeMillis = newDueTime,
            onDueTimeChange = {
                newDueTime = it
                if (it != null) {
                    weatherViewModel?.loadTaskWeather(it, newTaskType)
                }
            },
            endTimeMillis = newEndTime,
            onEndTimeChange = { newEndTime = it },
            notificationEnabled = newNotificationEnabled,
            onNotificationEnabledChange = { enabled ->
                if (enabled) {
                    val hasPermission = NotificationPermissionHelper.hasNotificationPermission(context)
                    val hasExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.canScheduleExactAlarms()
                    } else {
                        true
                    }

                    if (!hasPermission) {
                        val activity = NotificationPermissionHelper.findActivity(context)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (NotificationPermissionHelper.shouldShowSettingsRedirect(activity, prefs)) {
                                showNotificationSettingsDialog = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                                showNotificationSettingsDialog = true
                            } else {
                                newNotificationEnabled = true
                            }
                        }
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
            tags = newTags,
            onTagsChange = { newTags = it },
            recurrence = newRecurrence,
            onRecurrenceChange = { newRecurrence = it },
            weatherUiState = weatherViewModel?.taskWeatherState ?: WeatherUiState.Idle,
            onCheckWeatherClick = {
                weatherViewModel?.loadTaskWeather(newDueTime, newTaskType)
            },
            onNavigateToSettings = {
                showAddDialog = false
                previousScreen = if (currentScreen in listOf(Screen.SETTINGS, Screen.PROFILE, Screen.EDIT_PROFILE, Screen.TASK_DETAILS)) previousScreen else currentScreen
                currentScreen = Screen.SETTINGS
            },
            onConfirm = {
                if (newNotificationEnabled && newDueTime == null) {
                    validationErrorTitle = context.getString(R.string.invalid_time)
                    validationErrorMessage = context.getString(R.string.set_date_time)
                    showValidationErrorDialog = true
                } else if (newDueTime != null && newEndTime != null && newEndTime!! <= newDueTime!!) {
                    validationErrorTitle = context.getString(R.string.invalid_time)
                    validationErrorMessage = context.getString(R.string.end_time_before_start)
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
                                newNotificationMinutesBefore,
                                newTaskType,
                                newTags,
                                newRecurrence
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
                            newNotificationMinutesBefore,
                            newTaskType,
                            newTags,
                            newRecurrence
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
            taskType = editTaskType,
            onTaskTypeChange = {
                editTaskType = it
                if (editDueTime != null) {
                    weatherViewModel?.loadTaskWeather(editDueTime, it)
                }
            },
            dueTimeMillis = editDueTime,
            onDueTimeChange = {
                editDueTime = it
                if (it != null) {
                    weatherViewModel?.loadTaskWeather(it, editTaskType)
                }
            },
            endTimeMillis = editEndTime,
            onEndTimeChange = { editEndTime = it },
            notificationEnabled = editNotificationEnabled,
            onNotificationEnabledChange = { enabled ->
                if (enabled) {
                    val hasPermission = NotificationPermissionHelper.hasNotificationPermission(context)
                    val hasExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.canScheduleExactAlarms()
                    } else {
                        true
                    }

                    if (!hasPermission) {
                        val activity = NotificationPermissionHelper.findActivity(context)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (NotificationPermissionHelper.shouldShowSettingsRedirect(activity, prefs)) {
                                showNotificationSettingsDialog = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                                showNotificationSettingsDialog = true
                            } else {
                                editNotificationEnabled = true
                            }
                        }
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
            tags = editTags,
            onTagsChange = { editTags = it },
            recurrence = editRecurrence,
            onRecurrenceChange = { editRecurrence = it },
            createdAt = editingTodo?.createdAt ?: System.currentTimeMillis(),
            weatherUiState = weatherViewModel?.taskWeatherState ?: WeatherUiState.Idle,
            onCheckWeatherClick = {
                weatherViewModel?.loadTaskWeather(editDueTime, editTaskType)
            },
            onNavigateToSettings = {
                showEditDialog = false
                previousScreen = if (currentScreen in listOf(Screen.SETTINGS, Screen.PROFILE, Screen.EDIT_PROFILE, Screen.TASK_DETAILS)) previousScreen else currentScreen
                currentScreen = Screen.SETTINGS
            },
            onConfirm = {
                if (editNotificationEnabled && editDueTime == null) {
                    validationErrorTitle = context.getString(R.string.invalid_time)
                    validationErrorMessage = context.getString(R.string.set_date_time)
                    showValidationErrorDialog = true
                } else if (editDueTime != null && editEndTime != null && editEndTime!! <= editDueTime!!) {
                    validationErrorTitle = context.getString(R.string.invalid_time)
                    validationErrorMessage = context.getString(R.string.end_time_before_start)
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
                                    editNotificationMinutesBefore,
                                    editTaskType,
                                    editTags,
                                    editRecurrence
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
                                editNotificationMinutesBefore,
                                editTaskType,
                                editTags,
                                editRecurrence
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

    if (showReschedulePickerForDetails && selectedTodoForDetails != null) {
        val currentTask = selectedTodoForDetails!!
        CustomDateTimePickerDialog(
            initialTime = currentTask.dueTimeMillis ?: currentTask.createdAt,
            onDismiss = { showReschedulePickerForDetails = false },
            onSave = { newStart ->
                val duration = if (currentTask.dueTimeMillis != null && currentTask.endTimeMillis != null) {
                    currentTask.endTimeMillis!! - currentTask.dueTimeMillis!!
                } else {
                    60 * 60 * 1000L
                }
                val newEnd = newStart + duration
                val conflict = viewModel.checkTimeConflict(newStart, newEnd, currentTask.id)
                if (conflict != null) {
                    conflictingTodo = conflict
                    conflictResolutionCallback = {
                        viewModel.updateTodo(
                            todo = currentTask,
                            newTitle = currentTask.title,
                            newDescription = currentTask.description,
                            newPriority = currentTask.priority,
                            newDueTimeMillis = newStart,
                            newEndTimeMillis = newEnd,
                            newAttachmentUri = currentTask.attachmentUri,
                            newNotificationEnabled = currentTask.notificationEnabled,
                            newNotificationMinutesBefore = currentTask.notificationMinutesBefore,
                            newTaskType = currentTask.taskType
                        )
                        selectedTodoForDetails = currentTask.copy(dueTimeMillis = newStart, endTimeMillis = newEnd)
                        weatherViewModel?.loadTaskWeather(newStart, currentTask.taskType)
                        showReschedulePickerForDetails = false
                    }
                    showConflictDialog = true
                } else {
                    viewModel.updateTodo(
                        todo = currentTask,
                        newTitle = currentTask.title,
                        newDescription = currentTask.description,
                        newPriority = currentTask.priority,
                        newDueTimeMillis = newStart,
                        newEndTimeMillis = newEnd,
                        newAttachmentUri = currentTask.attachmentUri,
                        newNotificationEnabled = currentTask.notificationEnabled,
                        newNotificationMinutesBefore = currentTask.notificationMinutesBefore,
                        newTaskType = currentTask.taskType
                    )
                    selectedTodoForDetails = currentTask.copy(dueTimeMillis = newStart, endTimeMillis = newEnd)
                    weatherViewModel?.loadTaskWeather(newStart, currentTask.taskType)
                    showReschedulePickerForDetails = false
                }
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
            onSessionComplete = { }
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
                    Text(stringResource(R.string.done), fontWeight = FontWeight.Bold)
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
                    text = stringResource(R.string.permission_required),
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDarkTheme)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.exact_alarm_permission_msg),
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
                                Toast.makeText(context, "Please enable exact alarms manually in Settings.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold)
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
                    Text(stringResource(R.string.cancel), color = textSecondaryFor(isDarkTheme))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = surfaceColorFor(isDarkTheme)
        )
    }

    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsDialog = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = surfaceColorFor(isDarkTheme),
            title = {
                Text(
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textPrimaryFor(isDarkTheme)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notifications_settings_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryFor(isDarkTheme)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationSettingsDialog = false
                        NotificationPermissionHelper.openNotificationSettings(context)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text(
                        text = stringResource(R.string.open_settings),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationSettingsDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = textMutedFor(isDarkTheme)
                    )
                }
            }
        )
    }

    if (showRescheduleConfirm && conflictingTodo != null) {
        AlertDialog(
            onDismissRequest = { showRescheduleConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.conflict_detected),
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDarkTheme)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.swap_confirm_msg, conflictingTodo!!.title),
                    color = textSecondaryFor(isDarkTheme)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newConflict = viewModel.checkTimeConflict(rescheduleNewStart, rescheduleNewEnd, excludeTaskId = conflictingTodo!!.id)
                        if (newConflict != null) {
                            conflictingTodo = newConflict
                            Toast.makeText(context, context.getString(R.string.time_slot_occupied_msg), Toast.LENGTH_LONG).show()
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
                    Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = textSecondaryFor(isDarkTheme))
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
                        validationErrorMessage = context.getString(R.string.end_time_before_start)
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