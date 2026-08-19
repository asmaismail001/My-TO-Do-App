package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.PriorityHigh
import com.example.mytodoapp.ui.PriorityLow
import com.example.mytodoapp.ui.PriorityMedium
import com.example.mytodoapp.ui.TextSecondary

@Composable
fun PriorityBadge(priority: Priority) {
    val (dotColor, label) = when (priority) {
        Priority.HIGH -> PriorityHigh to "High"
        Priority.MEDIUM -> PriorityMedium to "Medium"
        Priority.LOW -> PriorityLow to "Low"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(7.dp)
                .background(dotColor, CircleShape)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 5.dp))
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}