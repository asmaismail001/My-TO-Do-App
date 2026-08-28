package com.example.mytodoapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.notification.NotificationScheduler
import com.example.mytodoapp.repository.TodoRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.mytodoapp.util.PreferencesManager
import com.example.mytodoapp.util.TimeConflict
import java.io.File

enum class DashboardPeriod { DAILY, WEEKLY, MONTHLY }

enum class SwapTimeResult { SUCCESS, CONFLICT, INVALID }

data class MonthlyChartData(
    val segmentLabel: String,
    val completedCount: Int,
    val pendingCount: Int
)

data class PeriodStats(
    val totalCount: Int,
    val completedCount: Int,
    val pendingCount: Int,
    val completionPercentage: Int
)

data class DailyChartData(
    val dayName: String,
    val dayAbbreviation: String,
    val completedCount: Int,
    val pendingCount: Int,
    val date: Calendar
)

class TodoViewModel(
    private val repository: TodoRepository,
    private val appContext: Context
) : ViewModel() {

    private val prefsManager = PreferencesManager(appContext)

    var profilePictureUri by mutableStateOf<String?>(prefsManager.getProfilePictureUri())
        private set

    fun updateProfilePictureUri(uriStr: String?) {
        profilePictureUri = uriStr
        prefsManager.setProfilePictureUri(uriStr)
    }

    var dashboardDate by mutableStateOf<Calendar>(Calendar.getInstance())
        private set

    var dashboardPeriod by mutableStateOf(DashboardPeriod.DAILY)
        private set

    fun updateDashboardDate(date: Calendar) {
        dashboardDate = date
    }

    fun updateDashboardPeriod(period: DashboardPeriod) {
        dashboardPeriod = period
    }

    fun getStartOfWeek(date: Calendar): Calendar {
        val cal = date.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diff = if (dayOfWeek == Calendar.SUNDAY) {
            -6
        } else {
            Calendar.MONDAY - dayOfWeek
        }
        cal.add(Calendar.DAY_OF_MONTH, diff)
        return cal
    }

    fun getTasksForDate(date: Calendar): List<Todo> {
        return todoList.filter { todo ->
            val targetTime = todo.dueTimeMillis ?: todo.createdAt
            com.example.mytodoapp.util.CalendarUtil.isSameDay(targetTime, date.timeInMillis)
        }
    }

    fun getTasksForPeriod(period: DashboardPeriod, date: Calendar): List<Todo> {
        return when (period) {
            DashboardPeriod.DAILY -> getTasksForDate(date)
            DashboardPeriod.WEEKLY -> {
                val start = getStartOfWeek(date)
                val end = (start.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                todoList.filter { todo ->
                    val targetTime = todo.dueTimeMillis ?: todo.createdAt
                    targetTime >= start.timeInMillis && targetTime <= end.timeInMillis
                }
            }
            DashboardPeriod.MONTHLY -> {
                val year = date.get(Calendar.YEAR)
                val month = date.get(Calendar.MONTH)
                todoList.filter { todo ->
                    val targetTime = todo.dueTimeMillis ?: todo.createdAt
                    val todoCal = Calendar.getInstance().apply { timeInMillis = targetTime }
                    todoCal.get(Calendar.YEAR) == year && todoCal.get(Calendar.MONTH) == month
                }
            }
        }
    }

    fun getStatsForPeriod(period: DashboardPeriod, date: Calendar): PeriodStats {
        val tasks = getTasksForPeriod(period, date)
        val total = tasks.size
        val completed = tasks.count { it.completed }
        val pending = total - completed
        val percentage = if (total > 0) (completed * 100) / total else 0
        return PeriodStats(total, completed, pending, percentage)
    }

    fun getWeeklyChartData(date: Calendar): List<DailyChartData> {
        val start = getStartOfWeek(date)
        val dayNames = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val dayAbbrevs = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        return (0..6).map { offset ->
            val dayCal = (start.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, offset)
            }
            val dayTasks = getTasksForDate(dayCal)
            val completed = dayTasks.count { it.completed }
            val pending = dayTasks.size - completed
            DailyChartData(
                dayName = dayNames[offset],
                dayAbbreviation = dayAbbrevs[offset],
                completedCount = completed,
                pendingCount = pending,
                date = dayCal
            )
        }
    }

    fun getMonthlyChartData(date: Calendar): List<MonthlyChartData> {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH)

        val monthTasks = todoList.filter { todo ->
            val targetTime = todo.dueTimeMillis ?: todo.createdAt
            val todoCal = Calendar.getInstance().apply { timeInMillis = targetTime }
            todoCal.get(Calendar.YEAR) == year && todoCal.get(Calendar.MONTH) == month
        }

        val segments = listOf(
            "1-7" to 1..7,
            "8-14" to 8..14,
            "15-21" to 15..21,
            "22+" to 22..31
        )

        return segments.map { (label, dayRange) ->
            val segmentTasks = monthTasks.filter { todo ->
                val targetTime = todo.dueTimeMillis ?: todo.createdAt
                val todoCal = Calendar.getInstance().apply { timeInMillis = targetTime }
                todoCal.get(Calendar.DAY_OF_MONTH) in dayRange
            }
            val completed = segmentTasks.count { it.completed }
            val pending = segmentTasks.size - completed
            MonthlyChartData(label, completed, pending)
        }
    }

    var todoList by mutableStateOf<List<Todo>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    val allTasks: List<Todo>
        get() = applySearch(todoList)

    val completedTasks: List<Todo>
        get() = applySearch(todoList.filter { it.completed })

    val pendingTasks: List<Todo>
        get() = applySearch(todoList.filter { !it.completed })

    init {
        loadTodos()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    private fun applySearch(list: List<Todo>): List<Todo> {
        if (searchQuery.isBlank()) return list
        val query = searchQuery.trim().lowercase()
        return list.filter {
            it.title.lowercase().contains(query) ||
                    it.description.lowercase().contains(query) ||
                    it.priority.name.lowercase().contains(query)
        }
    }

    fun loadTodos() {
        viewModelScope.launch {
            todoList = repository.getTodos()
            rescheduleEnabledReminders(todoList)
        }
    }

    private fun saveAttachmentLocally(context: Context, sourceUriStr: String): String? {
        try {
            val uri = Uri.parse(sourceUriStr)
            if (uri.scheme == "file" && uri.path?.contains(context.filesDir.absolutePath) == true) {
                return sourceUriStr
            }
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val attachmentsDir = File(context.filesDir, "attachments")
            if (!attachmentsDir.exists()) {
                attachmentsDir.mkdirs()
            }
            val fileExtension = getFileExtension(context, uri) ?: "jpg"
            val destFile = File(attachmentsDir, "attachment_${System.currentTimeMillis()}.${fileExtension}")
            destFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            return Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        return if (uri.scheme == android.content.ContentResolver.SCHEME_CONTENT) {
            val mimeType = context.contentResolver.getType(uri)
            android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        } else {
            android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        }
    }

    private fun deleteAttachmentLocally(context: Context, uriStr: String?) {
        if (uriStr == null) return
        try {
            val uri = Uri.parse(uriStr)
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null && path.contains(context.filesDir.absolutePath)) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkTimeConflict(startTime: Long?, endTime: Long?, excludeTaskId: Int = 0): Todo? {
        return TimeConflict.findOverlappingTask(
            startTime,
            endTime,
            todoList,
            excludeTaskId
        )
    }

    fun eligibleSwapTasks(forTask: Todo): List<Todo> {
        if (forTask.completed || forTask.dueTimeMillis == null || forTask.endTimeMillis == null) {
            return emptyList()
        }
        return todoList
            .filter { other ->
                !other.completed &&
                    other.id != forTask.id &&
                    other.dueTimeMillis != null &&
                    other.endTimeMillis != null
            }
            .sortedBy { it.dueTimeMillis }
    }

    fun swapTimeSlots(source: Todo, other: Todo, onComplete: (SwapTimeResult) -> Unit) {
        viewModelScope.launch {
            val latest = repository.getTodos()
            val taskA = latest.find { it.id == source.id }
            val taskB = latest.find { it.id == other.id }
            if (taskA == null || taskB == null ||
                taskA.completed || taskB.completed ||
                taskA.dueTimeMillis == null || taskA.endTimeMillis == null ||
                taskB.dueTimeMillis == null || taskB.endTimeMillis == null
            ) {
                onComplete(SwapTimeResult.INVALID)
                return@launch
            }

            if (TimeConflict.findSwapConflict(taskA, taskB, latest) != null) {
                onComplete(SwapTimeResult.CONFLICT)
                return@launch
            }

            val swappedA = taskA.copy(
                dueTimeMillis = taskB.dueTimeMillis,
                endTimeMillis = taskB.endTimeMillis
            )
            val swappedB = taskB.copy(
                dueTimeMillis = taskA.dueTimeMillis,
                endTimeMillis = taskA.endTimeMillis
            )

            repository.swapTodoTimes(swappedA, swappedB)
            rescheduleReminderFor(swappedA)
            rescheduleReminderFor(swappedB)
            todoList = repository.getTodos()
            onComplete(SwapTimeResult.SUCCESS)
        }
    }

    private fun rescheduleReminderFor(todo: Todo) {
        NotificationScheduler.cancelReminder(appContext, todo.id)
        if (!todo.completed &&
            todo.notificationEnabled &&
            (todo.dueTimeMillis != null || todo.endTimeMillis != null)
        ) {
            NotificationScheduler.scheduleReminder(
                appContext,
                todo.id,
                todo.title,
                todo.dueTimeMillis ?: 0L,
                todo.endTimeMillis,
                todo.notificationMinutesBefore
            )
        }
    }

    fun addTodo(
        title: String,
        description: String,
        priority: Priority,
        dueTimeMillis: Long?,
        endTimeMillis: Long? = null,
        attachmentUri: String? = null,
        notificationEnabled: Boolean = false,
        notificationMinutesBefore: Int = 10
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val localUriStr = attachmentUri?.let { saveAttachmentLocally(appContext, it) }
            val savedTodo = repository.addTodo(
                title.trim(),
                description.trim(),
                priority,
                dueTimeMillis,
                endTimeMillis,
                localUriStr,
                notificationEnabled,
                notificationMinutesBefore
            )
            if (notificationEnabled && (dueTimeMillis != null || endTimeMillis != null)) {
                android.util.Log.d(
                    "TodoViewModel",
                    "Task created id=${savedTodo.id} notificationEnabled=true " +
                        "start=$dueTimeMillis minutesBefore=$notificationMinutesBefore"
                )
                NotificationScheduler.scheduleReminder(
                    appContext,
                    savedTodo.id,
                    savedTodo.title,
                    dueTimeMillis ?: 0L,
                    endTimeMillis,
                    notificationMinutesBefore
                )
            } else {
                android.util.Log.d(
                    "TodoViewModel",
                    "Task created id=${savedTodo.id} notificationEnabled=$notificationEnabled start=$dueTimeMillis"
                )
            }
            loadTodos()
        }
    }

    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            repository.toggleTodo(todo)
            if (!todo.completed) {
                // If it was incomplete, it is now completed -> cancel the reminder
                NotificationScheduler.cancelReminder(appContext, todo.id)
            } else {
                // If it was completed, it is now incomplete -> reschedule if enabled
                if (todo.notificationEnabled && (todo.dueTimeMillis != null || todo.endTimeMillis != null)) {
                    NotificationScheduler.scheduleReminder(
                        appContext,
                        todo.id,
                        todo.title,
                        todo.dueTimeMillis ?: 0L,
                        todo.endTimeMillis,
                        todo.notificationMinutesBefore
                    )
                }
            }
            loadTodos()
        }
    }

    fun updateTodo(
        todo: Todo,
        newTitle: String,
        newDescription: String,
        newPriority: Priority,
        newDueTimeMillis: Long?,
        newEndTimeMillis: Long?,
        newAttachmentUri: String? = todo.attachmentUri,
        newNotificationEnabled: Boolean = todo.notificationEnabled,
        newNotificationMinutesBefore: Int = todo.notificationMinutesBefore
    ) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            val finalAttachmentUri = if (newAttachmentUri != todo.attachmentUri) {
                deleteAttachmentLocally(appContext, todo.attachmentUri)
                newAttachmentUri?.let { saveAttachmentLocally(appContext, it) }
            } else {
                newAttachmentUri
            }
            repository.updateTodo(
                todo,
                newTitle.trim(),
                newDescription.trim(),
                newPriority,
                newDueTimeMillis,
                newEndTimeMillis,
                finalAttachmentUri,
                newNotificationEnabled,
                newNotificationMinutesBefore
            )
            NotificationScheduler.cancelReminder(appContext, todo.id)
            if (newNotificationEnabled && (newDueTimeMillis != null || newEndTimeMillis != null)) {
                NotificationScheduler.scheduleReminder(
                    appContext,
                    todo.id,
                    newTitle.trim(),
                    newDueTimeMillis ?: 0L,
                    newEndTimeMillis,
                    newNotificationMinutesBefore
                )
            }
            loadTodos()
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            NotificationScheduler.cancelReminder(appContext, todo.id)
            deleteAttachmentLocally(appContext, todo.attachmentUri)
            repository.deleteTodo(todo)
            loadTodos()
        }
    }

    fun onGlobalNotificationsChanged(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) {
                todoList.forEach { NotificationScheduler.cancelReminder(appContext, it.id) }
            } else {
                rescheduleEnabledReminders(todoList)
            }
        }
    }

    private fun rescheduleEnabledReminders(todos: List<Todo>) {
        todos.forEach { todo ->
            if (!todo.completed && todo.notificationEnabled && (todo.dueTimeMillis != null || todo.endTimeMillis != null)) {
                NotificationScheduler.scheduleReminder(
                    appContext,
                    todo.id,
                    todo.title,
                    todo.dueTimeMillis ?: 0L,
                    todo.endTimeMillis,
                    todo.notificationMinutesBefore
                )
            }
        }
    }

    fun exportTasks(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            try {
                val json = Gson().toJson(todoList)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
            } catch (_: Exception) {
                // export failed silently
            }
        }
    }

    fun importTasks(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }
                if (json != null) {
                    val listType = object : TypeToken<List<Todo>>() {}.type
                    val imported: List<Todo> = Gson().fromJson(json, listType)
                    imported.forEach {
                        val saved = repository.addTodo(
                            title = it.title,
                            description = it.description,
                            priority = it.priority,
                            dueTimeMillis = it.dueTimeMillis,
                            endTimeMillis = it.endTimeMillis,
                            attachmentUri = it.attachmentUri,
                            notificationEnabled = it.notificationEnabled,
                            notificationMinutesBefore = if (it.notificationMinutesBefore > 0) it.notificationMinutesBefore else 10
                        )
                        if (saved.notificationEnabled && !saved.completed &&
                            (saved.endTimeMillis != null || saved.dueTimeMillis != null)
                        ) {
                            NotificationScheduler.scheduleReminder(
                                appContext,
                                saved.id,
                                saved.title,
                                saved.dueTimeMillis ?: 0L,
                                saved.endTimeMillis,
                                saved.notificationMinutesBefore
                            )
                        }
                    }
                    loadTodos()
                }
            } catch (_: Exception) {
                // import failed silently
            }
        }
    }
}

class TodoViewModelFactory(
    private val repository: TodoRepository,
    private val appContext: Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TodoViewModel(repository, appContext) as T
    }
}