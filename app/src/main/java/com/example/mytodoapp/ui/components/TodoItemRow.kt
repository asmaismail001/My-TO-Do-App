package com.example.mytodoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.PriorityHigh
import com.example.mytodoapp.ui.PriorityLow
import com.example.mytodoapp.ui.PriorityMedium
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor
import com.example.mytodoapp.util.DateTimePickerUtil

@Composable
fun TodoItemRow(
    todo: Todo,
    onToggle: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFocusClick: () -> Unit,
    onTodoClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val formatDateTime = remember {
        { millis: Long ->
            val sdf = java.text.SimpleDateFormat("d MMM • h:mm a", java.util.Locale.getDefault())
            sdf.format(java.util.Date(millis))
        }
    }

    val cardAlpha = if (todo.completed) 0.65f else 1.0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(vertical = 5.dp)
            .alpha(cardAlpha),
        color = surfaceColorFor(isDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f))
    ) {
        Box(modifier = Modifier.fillMaxSize().clickable { onTodoClick() }) {
            // Content Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Custom Rounded Checkbox
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = if (todo.completed) Accent else textMutedFor(isDark),
                            shape = CircleShape
                        )
                        .background(if (todo.completed) Accent else Color.Transparent)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (todo.completed) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Task details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PriorityDot(priority = todo.priority)
                        Text(
                            text = todo.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (todo.completed) textMutedFor(isDark) else textPrimaryFor(isDark),
                            textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (!todo.attachmentUri.isNullOrEmpty()) {
                            Text(
                                text = "📎",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                        if (todo.taskType != com.example.mytodoapp.model.TaskType.FLEXIBLE) {
                            Text(
                                text = if (todo.taskType == com.example.mytodoapp.model.TaskType.OUTDOOR) "🌲" else "🏠",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }

                    if (todo.description.isNotBlank()) {
                        Text(
                            text = todo.description,
                            color = textSecondaryFor(isDark),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Metadata row (Start Time and Due Time stacked vertically)
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Start Time Section
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = textMutedFor(isDark),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stringResource(R.string.start_time)}: ${formatDateTime(todo.dueTimeMillis ?: todo.createdAt)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = textSecondaryFor(isDark)
                            )
                        }

                        // Due Time Section (Thin, compact box below Start Time)
                        if (todo.endTimeMillis != null) {
                            val dueMillis = todo.endTimeMillis!!
                            val isOverdue = dueMillis < System.currentTimeMillis() && !todo.completed
                            val dueBg = if (isDark) {
                                Color(0xFFB8EDE4)
                            } else if (isOverdue) {
                                Color(0xFFFEE2E2)
                            } else {
                                Color(0xFFE0F2FE)
                            }
                            val dueText = if (isDark) {
                                Color(0xFF115E59)
                            } else if (isOverdue) {
                                Color(0xFFEF4444)
                            } else {
                                Color(0xFF0284C7)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = dueBg,
                                border = BorderStroke(0.5.dp, dueText.copy(alpha = 0.4f)),
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.due),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        color = dueText
                                    )
                                    Text(
                                        text = formatDateTime(dueMillis),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isDark) Color(0xFF134E4A) else Color(0xFF111827)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Focus Timer Action Button
                    if (!todo.completed) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { onFocusClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = stringResource(R.string.focus_timer),
                                tint = Accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Edit Action Button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit_task),
                            tint = textMutedFor(isDark),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Action Button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onDeleteClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.delete_task),
                            tint = textMutedFor(isDark),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}