package com.example.mytodoapp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.R
import com.example.mytodoapp.ui.DeleteRed
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor

@Composable
fun DeleteTaskDialog(
    taskTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = surfaceColorFor(isDark),
        title = {
            Text(
                text = stringResource(R.string.delete_task_confirm_title),
                fontWeight = FontWeight.Bold,
                color = textPrimaryFor(isDark)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_task_confirm_msg, taskTitle),
                color = textSecondaryFor(isDark)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete),
                    color = DeleteRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = textSecondaryFor(isDark)
                )
            }
        }
    )
}