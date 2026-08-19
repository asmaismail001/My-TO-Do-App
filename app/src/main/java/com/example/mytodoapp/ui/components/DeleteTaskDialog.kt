package com.example.mytodoapp.ui.components



import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.mytodoapp.ui.DeleteRed
import com.example.mytodoapp.ui.TextGrey

@Composable
fun DeleteTaskDialog(
    taskTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Task?", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to delete \"$taskTitle\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = DeleteRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGrey)
            }
        }
    )
}