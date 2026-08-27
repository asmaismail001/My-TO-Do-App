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
import com.example.mytodoapp.util.DateTimePickerUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTaskDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
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
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = surfaceColorFor(isDark),
            border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f)),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "New Task",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimaryFor(isDark),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    placeholder = { Text("Enter task title...", color = textMutedFor(isDark)) },
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    placeholder = { Text("Add more details...", color = textMutedFor(isDark)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
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

                Spacer(modifier = Modifier.height(18.dp))

                PrioritySelector(selected = priority, onSelect = onPriorityChange)

                Spacer(modifier = Modifier.height(18.dp))

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
                            text = "Task Schedule",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Accent
                        )

                        // Start Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    DateTimePickerUtil.pickDateTime(context, dueTimeMillis) { picked ->
                                        onDueTimeChange(picked)
                                        if (endTimeMillis == null) {
                                            onEndTimeChange(picked + 60 * 60 * 1000L)
                                        }
                                    }
                                }
                        ) {
                            Text(
                                text = "Start",
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
                                        text = dueTimeMillis?.let { formatDateOnly(it) } ?: "Set date",
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
                                        text = dueTimeMillis?.let { formatTimeOnly(it) } ?: "Set time",
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
                                    DateTimePickerUtil.pickDateTime(context, endTimeMillis) { picked ->
                                        onEndTimeChange(picked)
                                    }
                                }
                        ) {
                            Text(
                                text = "Due",
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
                                        text = endTimeMillis?.let { formatDateOnly(it) } ?: "Set date",
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
                                        text = endTimeMillis?.let { formatTimeOnly(it) } ?: "Set time",
                                        color = if (endTimeMillis != null) textPrimaryFor(isDark) else textSecondaryFor(isDark),
                                        fontWeight = if (endTimeMillis != null) FontWeight.SemiBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Task Notification",
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remind me",
                            color = textSecondaryFor(isDark),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        val options = listOf(
                            0 to "At start time",
                            1 to "1 minute before",
                            5 to "5 minutes before",
                            10 to "10 minutes before",
                            15 to "15 minutes before",
                            30 to "30 minutes before",
                            60 to "1 hour before",
                            120 to "2 hours before",
                            -1 to "Custom"
                        )
                        Box {
                            TextButton(
                                onClick = { dropdownExpanded = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Accent),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                val currentText = if (showCustomInput) {
                                    "Custom ▼"
                                } else {
                                    options.firstOrNull { it.first == notificationMinutesBefore }?.second ?: "${notificationMinutesBefore} minutes before ▼"
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
                                label = { Text("Minutes before") },
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
                        Text("Cancel", color = textSecondaryFor(isDark), fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("Add Task", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}