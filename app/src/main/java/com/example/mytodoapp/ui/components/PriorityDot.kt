package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.PriorityHigh
import com.example.mytodoapp.ui.PriorityLow
import com.example.mytodoapp.ui.PriorityMedium

@Composable
fun PriorityDot(priority: Priority) {
    val color = when (priority) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW -> PriorityLow
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(9.dp)
            .background(color, CircleShape)
    )
}