package com.example.mytodoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.AttachmentPicker
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor
import com.example.mytodoapp.model.TaskType
import com.example.mytodoapp.model.WeatherUiState
import com.example.mytodoapp.ui.weather.TaskTypeSelector
import com.example.mytodoapp.ui.weather.WeatherCard
import com.example.mytodoapp.util.DateTimePickerUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.example.mytodoapp.R

@Composable
fun AddTaskDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    tags: List<String> = emptyList(),
    onTagsChange: (List<String>) -> Unit = {},
    recurrence: com.example.mytodoapp.model.RecurrenceType = com.example.mytodoapp.model.RecurrenceType.NONE,
    onRecurrenceChange: (com.example.mytodoapp.model.RecurrenceType) -> Unit = {},
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
    weatherUiState: WeatherUiState = WeatherUiState.Idle,
    onCheckWeatherClick: (() -> Unit)? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    var showDateTimePicker by remember { mutableStateOf(false) }
    var dateTimePickerInitialTime by remember { mutableStateOf<Long?>(null) }
    var dateTimePickerOnPicked by remember { mutableStateOf<((Long) -> Unit)?>(null) }

    val formatDateOnly = remember {
        { millis: Long ->
            val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
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
            shape = RoundedCornerShape(24.dp),
            color = surfaceColorFor(isDark),
            border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f)),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.new_task),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimaryFor(isDark),
                    modifier = Modifier.padding(bottom = 18.dp)
                )

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
                        unfocusedBorderColor = Accent.copy(alpha = 0.35f),
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = textSecondaryFor(isDark),
                        focusedTextColor = textPrimaryFor(isDark),
                        unfocusedTextColor = textPrimaryFor(isDark),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.task_desc)) },
                    placeholder = { Text(stringResource(R.string.task_desc_hint), color = textMutedFor(isDark)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Accent.copy(alpha = 0.35f),
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = textSecondaryFor(isDark),
                        focusedTextColor = textPrimaryFor(isDark),
                        unfocusedTextColor = textPrimaryFor(isDark),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrioritySelector(selected = priority, onSelect = onPriorityChange)

                Spacer(modifier = Modifier.height(16.dp))

                TagInputSection(
                    tags = tags,
                    onTagsChange = onTagsChange,
                    isDark = isDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                RecurrenceSelector(
                    selected = recurrence,
                    onSelect = onRecurrenceChange,
                    isDark = isDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                TaskTypeSelector(selectedType = taskType, onTypeSelected = onTaskTypeChange)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f)),
                    colors = CardDefaults.outlinedCardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8F9FA))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.task_schedule),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Accent
                        )

                        // Start Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
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
                            Text(
                                text = stringResource(R.string.start),
                                style = MaterialTheme.typography.labelMedium,
                                color = textSecondaryFor(isDark),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = if (dueTimeMillis != null) Accent else textSecondaryFor(isDark),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dueTimeMillis?.let { formatDateOnly(it) } ?: stringResource(R.string.set_date),
                                        color = if (dueTimeMillis != null) textPrimaryFor(isDark) else textSecondaryFor(isDark),
                                        fontWeight = if (dueTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = if (dueTimeMillis != null) Accent else textSecondaryFor(isDark),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dueTimeMillis?.let { formatTimeOnly(it) } ?: stringResource(R.string.set_time),
                                        color = if (dueTimeMillis != null) textPrimaryFor(isDark) else textSecondaryFor(isDark),
                                        fontWeight = if (dueTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Accent.copy(alpha = 0.15f), thickness = 1.dp)

                        // Due Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dateTimePickerInitialTime = endTimeMillis
                                    dateTimePickerOnPicked = { picked ->
                                        onEndTimeChange(picked)
                                    }
                                    showDateTimePicker = true
                                }
                        ) {
                            Text(
                                text = stringResource(R.string.due),
                                style = MaterialTheme.typography.labelMedium,
                                color = textSecondaryFor(isDark),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = if (endTimeMillis != null) Accent else textSecondaryFor(isDark),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = endTimeMillis?.let { formatDateOnly(it) } ?: stringResource(R.string.set_date),
                                        color = if (endTimeMillis != null) textPrimaryFor(isDark) else textSecondaryFor(isDark),
                                        fontWeight = if (endTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = if (endTimeMillis != null) Accent else textSecondaryFor(isDark),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = endTimeMillis?.let { formatTimeOnly(it) } ?: stringResource(R.string.set_time),
                                        color = if (endTimeMillis != null) textPrimaryFor(isDark) else textSecondaryFor(isDark),
                                        fontWeight = if (endTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                if (dueTimeMillis != null) {
                    Spacer(modifier = Modifier.height(14.dp))
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

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.task_notification),
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimaryFor(isDark),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = onNotificationEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Accent,
                            uncheckedThumbColor = textSecondaryFor(isDark),
                            uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF3F4F6)
                        )
                    )
                }

                if (notificationEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    var showCustomInput by remember { mutableStateOf(notificationMinutesBefore !in listOf(0, 1, 5, 10, 15, 30, 60, 120)) }
                    var customInputText by remember { mutableStateOf(if (showCustomInput) notificationMinutesBefore.toString() else "") }

                    val atStartStr = stringResource(R.string.at_start_time)
                    val min1Str = stringResource(R.string.min_before_1)
                    val min5Str = stringResource(R.string.min_before_5)
                    val min10Str = stringResource(R.string.min_before_10)
                    val min15Str = stringResource(R.string.min_before_15)
                    val min30Str = stringResource(R.string.min_before_30)
                    val hour1Str = stringResource(R.string.hour_before_1)
                    val hours2Str = stringResource(R.string.hours_before_2)
                    val customStr = stringResource(R.string.custom)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.remind_me),
                            color = textSecondaryFor(isDark),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        val options = listOf(
                            0 to atStartStr,
                            1 to min1Str,
                            5 to min5Str,
                            10 to min10Str,
                            15 to min15Str,
                            30 to min30Str,
                            60 to hour1Str,
                            120 to hours2Str,
                            -1 to customStr
                        )
                        Box {
                            TextButton(
                                onClick = { dropdownExpanded = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Accent),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                val currentText = if (showCustomInput) {
                                    "$customStr ▼"
                                } else {
                                    options.firstOrNull { it.first == notificationMinutesBefore }?.second ?: "$notificationMinutesBefore $min1Str ▼"
                                }
                                val displayText = if (currentText.endsWith("▼")) currentText else "$currentText ▼"
                                Text(text = displayText, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(surfaceColorFor(isDark))
                            ) {
                                options.forEach { (minutes, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label, color = textPrimaryFor(isDark)) },
                                        onClick = {
                                             if (minutes == -1) {
                                                showCustomInput = true
                                            } else {
                                                showCustomInput = false
                                                onNotificationMinutesBeforeChange(minutes)
                                            }
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (showCustomInput) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
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
                                modifier = Modifier.width(180.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Accent,
                                    unfocusedBorderColor = Accent.copy(alpha = 0.35f),
                                    focusedLabelColor = Accent,
                                    unfocusedLabelColor = textSecondaryFor(isDark),
                                    focusedTextColor = textPrimaryFor(isDark),
                                    unfocusedTextColor = textPrimaryFor(isDark),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                AttachmentPicker(
                    attachmentUri = attachmentUri,
                    onAttachmentChanged = onAttachmentChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.cancel), color = textSecondaryFor(isDark), fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(stringResource(R.string.add_task), fontWeight = FontWeight.Bold)
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