package com.example.mytodoapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.TextMuted
import com.example.mytodoapp.ui.TextPrimary
import com.example.mytodoapp.ui.TextSecondary
import com.example.mytodoapp.ui.taskColorFor
import com.example.mytodoapp.util.DateTimePickerUtil

@Composable
fun TodoItemRow(
    todo: Todo,
    onToggle: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cardColor = taskColorFor(todo.id, todo.completed)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onEditClick() },
        color = cardColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = todo.completed,
                onCheckedChange = { onToggle() },
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                if (todo.description.isNotBlank()) {
                    Text(
                        text = todo.description,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(priority = todo.priority)

                    if (todo.dueTimeMillis != null) {
                        Text(
                            text = "  •  Due ${DateTimePickerUtil.formatDateTime(todo.dueTimeMillis)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = "Added ${DateTimePickerUtil.formatDateTime(todo.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            IconButton(onClick = onDeleteClick, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Delete Task", tint = TextMuted)
            }
        }
    }
}