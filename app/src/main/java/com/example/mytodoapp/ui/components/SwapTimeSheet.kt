package com.example.mytodoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SwapTimeActionButton(
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.SwapHoriz,
            contentDescription = "Swap Time",
            tint = Accent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = "Swap Time",
            color = Accent,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapTimePickerSheet(
    isDark: Boolean,
    candidates: List<Todo>,
    onSelect: (Todo) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceColorFor(isDark),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Swap Time",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = textPrimaryFor(isDark)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select a task to swap with:",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondaryFor(isDark)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (candidates.isEmpty()) {
                Text(
                    text = "No other scheduled tasks are available to swap with.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryFor(isDark)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    items(candidates, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(task) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8F9FA)
                            ),
                            border = BorderStroke(1.dp, cardBorderColorFor(isDark))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimaryFor(isDark)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatSwapTimeRange(task.dueTimeMillis, task.endTimeMillis),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondaryFor(isDark)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwapTimeConfirmDialog(
    isDark: Boolean,
    source: Todo,
    other: Todo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "Swap time?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimaryFor(isDark)
                )
                Spacer(modifier = Modifier.height(16.dp))
                SwapPreviewCard(todo = source, isDark = isDark)
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 10.dp)
                        .size(22.dp)
                )
                SwapPreviewCard(todo = other, isDark = isDark)
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = textSecondaryFor(isDark))
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text("Swap", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SwapPreviewCard(todo: Todo, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8F9FA)
        ),
        border = BorderStroke(1.dp, cardBorderColorFor(isDark))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = todo.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = textPrimaryFor(isDark)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatSwapTimeRange(todo.dueTimeMillis, todo.endTimeMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondaryFor(isDark)
            )
        }
    }
}

fun formatSwapTimeRange(startMillis: Long?, endMillis: Long?): String {
    if (startMillis == null || startMillis <= 0) return ""
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    val startStr = timeFormat.format(Date(startMillis))
    val endStr = if (endMillis != null && endMillis > 0) timeFormat.format(Date(endMillis)) else ""
    val dateStr = dateFormat.format(Date(startMillis))
    val endDateStr = if (endMillis != null && endMillis > 0) dateFormat.format(Date(endMillis)) else dateStr
    return if (endStr.isNotEmpty()) {
        if (dateStr == endDateStr) {
            "$dateStr, $startStr – $endStr"
        } else {
            "$dateStr, $startStr – $endDateStr, $endStr"
        }
    } else {
        "$dateStr, $startStr"
    }
}
