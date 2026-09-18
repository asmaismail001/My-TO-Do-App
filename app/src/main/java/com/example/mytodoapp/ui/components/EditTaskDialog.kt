package com.example.mytodoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mytodoapp.R
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.RecurrenceType
import com.example.mytodoapp.model.TaskType
import com.example.mytodoapp.model.WeatherUiState
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.AttachmentPicker
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor
import com.example.mytodoapp.ui.weather.TaskTypeSelector
import com.example.mytodoapp.ui.weather.WeatherCard
import com.example.mytodoapp.util.DateTimePickerUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditTaskDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    tags: List<String> = emptyList(),
    onTagsChange: (List<String>) -> Unit = {},
    recurrence: RecurrenceType = RecurrenceType.NONE,
    onRecurrenceChange: (RecurrenceType) -> Unit = {},
    taskType: TaskType = TaskType.FLEXIBLE,
    onTaskTypeChange: (TaskType) -> Unit = {},
    dueTimeMillis: Long?,
    onDueTimeChange: (Long?) -> Unit,
    endTimeMillis: Long?,
    onEndTimeChange: (Long?) -> Unit,
    notificationEnabled: Boolean,
    onNotificationEnabledChange: (Boolean) -> Unit,
    notificationMinutesBefore: Int,
    onNotificationMinutesBeforeChange: (Int) -> Unit,
    attachmentUri: String?,
    onAttachmentChange: (String?) -> Unit,
    createdAt: Long,
    weatherUiState: WeatherUiState = WeatherUiState.Idle,
    onCheckWeatherClick: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    var showDateTimePicker by remember { mutableStateOf(false) }
    var dateTimePickerInitialTime by remember { mutableStateOf<Long?>(null) }
    var dateTimePickerOnPicked by remember { mutableStateOf<((Long) -> Unit)?>(null) }

    val hasActiveAdvancedOptions = tags.isNotEmpty() ||
            recurrence != RecurrenceType.NONE ||
            taskType != TaskType.FLEXIBLE ||
            !attachmentUri.isNullOrEmpty() ||
            notificationEnabled

    var isAdvancedExpanded by rememberSaveable { mutableStateOf(hasActiveAdvancedOptions) }

    val formatDateOnly = remember {
        { millis: Long ->
            val sdf = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            sdf.format(Date(millis))
        }
    }

    val formatTimeOnly = remember {
        { millis: Long ->
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(millis))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = surfaceColorFor(isDark),
            border = BorderStroke(1.dp, cardBorderColorFor(isDark)),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header: Title, Creation Date & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.edit_task),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                                color = textPrimaryFor(isDark)
                            )
                            Text(
                                text = stringResource(R.string.added_date, DateTimePickerUtil.formatDateTime(createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = textMutedFor(isDark)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = textSecondaryFor(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Task Title (Clean, Compact, Easy to Type)
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.task_title)) },
                    placeholder = { Text(stringResource(R.string.task_title_hint), color = textMutedFor(isDark)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = cardBorderColorFor(isDark),
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = textSecondaryFor(isDark),
                        focusedTextColor = textPrimaryFor(isDark),
                        unfocusedTextColor = textPrimaryFor(isDark),
                        focusedContainerColor = if (isDark) Color(0xFF182026) else Color(0xFFF9FAFB),
                        unfocusedContainerColor = if (isDark) Color(0xFF182026) else Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Description (Compact)
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.task_desc)) },
                    placeholder = { Text(stringResource(R.string.task_desc_hint), color = textMutedFor(isDark)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = cardBorderColorFor(isDark),
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = textSecondaryFor(isDark),
                        focusedTextColor = textPrimaryFor(isDark),
                        unfocusedTextColor = textPrimaryFor(isDark),
                        focusedContainerColor = if (isDark) Color(0xFF182026) else Color(0xFFF9FAFB),
                        unfocusedContainerColor = if (isDark) Color(0xFF182026) else Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Priority Selector (Compact dropdown with indicator dot)
                PrioritySelector(selected = priority, onSelect = onPriorityChange)

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Task Schedule (Single Unified, Compact Schedule Component)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF18222C) else Color(0xFFF4F7F6)
                    ),
                    border = BorderStroke(1.dp, cardBorderColorFor(isDark))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.task_schedule),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = textPrimaryFor(isDark)
                            )
                        }

                        // Start Row: Label + [Date Pill] [Time Pill]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.start),
                                style = MaterialTheme.typography.labelMedium,
                                color = Accent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(42.dp)
                            )

                            // Start Date Pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF11181F) else Color.White,
                                border = BorderStroke(1.dp, if (dueTimeMillis != null) Accent.copy(alpha = 0.45f) else cardBorderColorFor(isDark)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        dateTimePickerInitialTime = dueTimeMillis
                                        dateTimePickerOnPicked = { picked ->
                                            onDueTimeChange(picked)
                                            if (endTimeMillis == null) {
                                                onEndTimeChange(picked + 60 * 60 * 1000L)
                                            }
                                        }
                                        showDateTimePicker = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = if (dueTimeMillis != null) Accent else textMutedFor(isDark),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dueTimeMillis?.let { formatDateOnly(it) } ?: stringResource(R.string.set_date),
                                        color = if (dueTimeMillis != null) textPrimaryFor(isDark) else textMutedFor(isDark),
                                        fontWeight = if (dueTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Start Time Pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF11181F) else Color.White,
                                border = BorderStroke(1.dp, if (dueTimeMillis != null) Accent.copy(alpha = 0.45f) else cardBorderColorFor(isDark)),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .clickable {
                                        dateTimePickerInitialTime = dueTimeMillis
                                        dateTimePickerOnPicked = { picked ->
                                            onDueTimeChange(picked)
                                            if (endTimeMillis == null) {
                                                onEndTimeChange(picked + 60 * 60 * 1000L)
                                            }
                                        }
                                        showDateTimePicker = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = if (dueTimeMillis != null) Accent else textMutedFor(isDark),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = dueTimeMillis?.let { formatTimeOnly(it) } ?: stringResource(R.string.set_time),
                                        color = if (dueTimeMillis != null) textPrimaryFor(isDark) else textMutedFor(isDark),
                                        fontWeight = if (dueTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Due Row: Label + [Date Pill] [Time Pill]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.due),
                                style = MaterialTheme.typography.labelMedium,
                                color = textSecondaryFor(isDark),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(42.dp)
                            )

                            // Due Date Pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF11181F) else Color.White,
                                border = BorderStroke(1.dp, if (endTimeMillis != null) Accent.copy(alpha = 0.45f) else cardBorderColorFor(isDark)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        dateTimePickerInitialTime = endTimeMillis
                                        dateTimePickerOnPicked = { picked ->
                                            onEndTimeChange(picked)
                                        }
                                        showDateTimePicker = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = if (endTimeMillis != null) Accent else textMutedFor(isDark),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = endTimeMillis?.let { formatDateOnly(it) } ?: stringResource(R.string.set_date),
                                        color = if (endTimeMillis != null) textPrimaryFor(isDark) else textMutedFor(isDark),
                                        fontWeight = if (endTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Due Time Pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF11181F) else Color.White,
                                border = BorderStroke(1.dp, if (endTimeMillis != null) Accent.copy(alpha = 0.45f) else cardBorderColorFor(isDark)),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .clickable {
                                        dateTimePickerInitialTime = endTimeMillis
                                        dateTimePickerOnPicked = { picked ->
                                            onEndTimeChange(picked)
                                        }
                                        showDateTimePicker = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = if (endTimeMillis != null) Accent else textMutedFor(isDark),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = endTimeMillis?.let { formatTimeOnly(it) } ?: stringResource(R.string.set_time),
                                        color = if (endTimeMillis != null) textPrimaryFor(isDark) else textMutedFor(isDark),
                                        fontWeight = if (endTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Advanced Expandable Section Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF182026) else Color(0xFFF3F4F6),
                    border = BorderStroke(1.dp, if (hasActiveAdvancedOptions) Accent.copy(alpha = 0.5f) else cardBorderColorFor(isDark)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAdvancedExpanded = !isAdvancedExpanded }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = null,
                                tint = if (isAdvancedExpanded || hasActiveAdvancedOptions) Accent else textSecondaryFor(isDark),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Advanced",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isAdvancedExpanded || hasActiveAdvancedOptions) Accent else textPrimaryFor(isDark)
                            )
                            if (hasActiveAdvancedOptions && !isAdvancedExpanded) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Accent)
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isAdvancedExpanded) "Collapse" else "Expand",
                            tint = textSecondaryFor(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Collapsible Advanced Container (Preserving all functionality & state)
                AnimatedVisibility(
                    visible = isAdvancedExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Advanced: Tags
                        TagInputSection(
                            tags = tags,
                            onTagsChange = onTagsChange,
                            isDark = isDark
                        )

                        // Advanced: Repeat / Recurrence
                        RecurrenceSelector(
                            selected = recurrence,
                            onSelect = onRecurrenceChange,
                            isDark = isDark
                        )

                        // Advanced: Activity Location
                        TaskTypeSelector(selectedType = taskType, onTypeSelected = onTaskTypeChange)

                        // Advanced: Reminders & Notification Lead Time
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF18222C) else Color(0xFFF4F7F6)
                            ),
                            border = BorderStroke(1.dp, cardBorderColorFor(isDark))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .then(
                                                if (onNavigateToSettings != null) {
                                                    Modifier.clickable { onNavigateToSettings() }
                                                } else Modifier
                                            )
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (notificationEnabled) Accent.copy(alpha = 0.15f) else textMutedFor(isDark).copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Notifications,
                                                contentDescription = null,
                                                tint = if (notificationEnabled) Accent else textMutedFor(isDark),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = stringResource(R.string.task_notification),
                                                    fontWeight = FontWeight.Bold,
                                                    color = textPrimaryFor(isDark),
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                if (onNavigateToSettings != null) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Outlined.Settings,
                                                        contentDescription = stringResource(R.string.settings),
                                                        tint = Accent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (notificationEnabled) stringResource(R.string.remind_me) else stringResource(R.string.disabled),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textSecondaryFor(isDark)
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = notificationEnabled,
                                        onCheckedChange = onNotificationEnabledChange,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Accent,
                                            uncheckedThumbColor = textSecondaryFor(isDark),
                                            uncheckedTrackColor = if (isDark) Color(0xFF242E38) else Color(0xFFE5E7EB)
                                        )
                                    )
                                }

                                if (notificationEnabled) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = cardBorderColorFor(isDark), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    var showCustomInput by remember {
                                        mutableStateOf(notificationMinutesBefore !in listOf(0, 5, 10, 15, 30, 60, 120))
                                    }
                                    var customInputText by remember {
                                        mutableStateOf(if (showCustomInput) notificationMinutesBefore.toString() else "")
                                    }

                                    val leadOptions = listOf(
                                        0 to stringResource(R.string.at_start_time),
                                        5 to "5m",
                                        10 to "10m",
                                        15 to "15m",
                                        30 to "30m",
                                        60 to "1h",
                                        120 to "2h",
                                        -1 to stringResource(R.string.custom)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        leadOptions.forEach { (mins, label) ->
                                            val isSelected = if (mins == -1) showCustomInput else (!showCustomInput && notificationMinutesBefore == mins)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    if (mins == -1) {
                                                        showCustomInput = true
                                                    } else {
                                                        showCustomInput = false
                                                        onNotificationMinutesBeforeChange(mins)
                                                    }
                                                },
                                                label = {
                                                    Text(
                                                        text = label,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 12.sp
                                                    )
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Accent,
                                                    selectedLabelColor = Color.White,
                                                    containerColor = if (isDark) Color(0xFF11181F) else Color.White,
                                                    labelColor = textPrimaryFor(isDark)
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    borderColor = if (isSelected) Accent else cardBorderColorFor(isDark),
                                                    enabled = true,
                                                    selected = isSelected
                                                )
                                            )
                                        }
                                    }

                                    if (showCustomInput) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = customInputText,
                                            onValueChange = { newValue ->
                                                if (newValue.all { it.isDigit() }) {
                                                    customInputText = newValue
                                                    val mins = newValue.toIntOrNull() ?: 10
                                                    onNotificationMinutesBeforeChange(mins)
                                                }
                                            },
                                            label = { Text(stringResource(R.string.minutes_before)) },
                                            singleLine = true,
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Accent,
                                                unfocusedBorderColor = cardBorderColorFor(isDark),
                                                focusedLabelColor = Accent,
                                                unfocusedLabelColor = textSecondaryFor(isDark),
                                                focusedTextColor = textPrimaryFor(isDark),
                                                unfocusedTextColor = textPrimaryFor(isDark),
                                                focusedContainerColor = if (isDark) Color(0xFF11181F) else Color.White,
                                                unfocusedContainerColor = if (isDark) Color(0xFF11181F) else Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Advanced: Attachment
                        AttachmentPicker(
                            attachmentUri = attachmentUri,
                            onAttachmentChanged = onAttachmentChange
                        )

                        // Advanced: Weather Forecast Advice (if schedule is set)
                        if (dueTimeMillis != null) {
                            if (weatherUiState is WeatherUiState.Idle) {
                                OutlinedButton(
                                    onClick = { onCheckWeatherClick?.invoke() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Accent.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text("🌤 " + stringResource(R.string.check_weather_forecast), color = Accent, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                WeatherCard(
                                    weatherUiState = weatherUiState,
                                    taskType = taskType,
                                    onRefresh = { onCheckWeatherClick?.invoke() },
                                    title = stringResource(R.string.forecast_at_task_time)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Footer Actions: Cancel & Save Changes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = textSecondaryFor(isDark),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(stringResource(R.string.save_changes), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    if (showDateTimePicker) {
        CustomDateTimePickerDialog(
            initialTime = dateTimePickerInitialTime,
            onDismiss = { showDateTimePicker = false },
            onSave = { picked ->
                dateTimePickerOnPicked?.invoke(picked)
                showDateTimePicker = false
            }
        )
    }
}