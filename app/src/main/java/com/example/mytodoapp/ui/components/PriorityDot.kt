package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.TextMuted

import com.example.mytodoapp.ui.PriorityHigh
import com.example.mytodoapp.ui.PriorityMedium
import com.example.mytodoapp.ui.PriorityLow

@Composable
fun PriorityDot(priority: Priority) {
    when (priority) {
        Priority.HIGH -> androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(9.dp).background(PriorityHigh, CircleShape)
        )
        Priority.MEDIUM -> androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(9.dp).background(PriorityMedium, CircleShape)
        )
        Priority.LOW -> androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(9.dp).background(PriorityLow, CircleShape)
        )
    }
}