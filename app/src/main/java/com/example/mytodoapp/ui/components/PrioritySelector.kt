package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.PriorityHighBg
import com.example.mytodoapp.ui.PriorityHighText
import com.example.mytodoapp.ui.PriorityLowBg
import com.example.mytodoapp.ui.PriorityLowText
import com.example.mytodoapp.ui.PriorityMediumBg
import com.example.mytodoapp.ui.PriorityMediumText
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.textSecondaryFor

@Composable
fun PrioritySelector(
    selected: Priority,
    onSelect: (Priority) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Column {
        Text(
            text = "Priority",
            color = textSecondaryFor(isDark),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriorityOption(Priority.HIGH, "High", PriorityHighBg, PriorityHighText, selected, onSelect, isDark)
            PriorityOption(Priority.MEDIUM, "Medium", PriorityMediumBg, PriorityMediumText, selected, onSelect, isDark)
            PriorityOption(Priority.LOW, "Low", PriorityLowBg, PriorityLowText, selected, onSelect, isDark)
        }
    }
}

@Composable
private fun PriorityOption(
    value: Priority,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    selected: Priority,
    onSelect: (Priority) -> Unit,
    isDark: Boolean
) {
    val isSelected = selected == value
    val defaultBg = if (isDark) Color(0xFF222836) else Color(0xFFF3F4F6)
    val defaultBorder = cardBorderColorFor(isDark)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) backgroundColor else defaultBg)
            .border(
                width = 1.dp,
                color = if (isSelected) textColor.copy(alpha = 0.5f) else defaultBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelect(value) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) textColor else textSecondaryFor(isDark),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall
        )
    }
}