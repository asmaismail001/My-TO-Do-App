package com.example.mytodoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.DialogFieldBackground
import com.example.mytodoapp.ui.SageGreen
import com.example.mytodoapp.ui.TextMuted
import com.example.mytodoapp.ui.TextPrimary
import com.example.mytodoapp.ui.TextSecondary
import com.example.mytodoapp.util.DateTimePickerUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    dueTimeMillis: Long?,
    onDueTimeChange: (Long?) -> Unit,
    createdAt: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Edit Task",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Added: ${DateTimePickerUtil.formatDateTime(createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = { Text("Title", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DialogFieldBackground,
                        unfocusedContainerColor = DialogFieldBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    placeholder = { Text("Description", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DialogFieldBackground,
                        unfocusedContainerColor = DialogFieldBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                PrioritySelector(selected = priority, onSelect = onPriorityChange)

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    onClick = {
                        DateTimePickerUtil.pickDateTime(context) { picked -> onDueTimeChange(picked) }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = DialogFieldBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dueTimeMillis?.let { "Due: ${DateTimePickerUtil.formatDateTime(it)}" }
                            ?: "Add due date & time",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}