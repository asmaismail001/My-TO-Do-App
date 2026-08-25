package com.example.mytodoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConflictDialog(
    conflictingTodo: Todo,
    onDismiss: () -> Unit,
    onViewConflictingTask: () -> Unit,
    onRescheduleConflictingTask: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
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
            ) {
                Text(
                    text = "Time Slot Unavailable",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimaryFor(isDark)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This time slot is already occupied by another task.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryFor(isDark)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8F9FA)),
                    border = BorderStroke(1.dp, cardBorderColorFor(isDark))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = conflictingTodo.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textPrimaryFor(isDark)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatTimeRange(conflictingTodo.dueTimeMillis, conflictingTodo.endTimeMillis),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondaryFor(isDark)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Change Time", color = textSecondaryFor(isDark))
                        }
                        TextButton(
                            onClick = onViewConflictingTask,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View Task", color = Accent)
                        }
                    }
                    Button(
                        onClick = onRescheduleConflictingTask,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reschedule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatTimeRange(startMillis: Long?, endMillis: Long?): String {
    if (startMillis == null || startMillis <= 0) return ""
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    val startStr = timeFormat.format(Date(startMillis))
    val endStr = if (endMillis != null && endMillis > 0) timeFormat.format(Date(endMillis)) else ""
    val dateStr = dateFormat.format(Date(startMillis))
    return if (endStr.isNotEmpty()) {
        "$dateStr, $startStr – $endStr"
    } else {
        "$dateStr, $startStr"
    }
}
