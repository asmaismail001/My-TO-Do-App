package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.PriorityHighBg
import com.example.mytodoapp.ui.PriorityHighText
import com.example.mytodoapp.ui.PriorityLowBg
import com.example.mytodoapp.ui.PriorityLowText
import com.example.mytodoapp.ui.PriorityMediumBg
import com.example.mytodoapp.ui.PriorityMediumText
import com.example.mytodoapp.ui.TextGrey

@Composable
fun PrioritySelector(
    selected: Priority,
    onSelect: (Priority) -> Unit
) {
    Column {
        Text(
            text = "Priority",
            color = TextGrey,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriorityOption(Priority.HIGH, "High", PriorityHighBg, PriorityHighText, selected, onSelect)
            PriorityOption(Priority.MEDIUM, "Medium", PriorityMediumBg, PriorityMediumText, selected, onSelect)
            PriorityOption(Priority.LOW, "Low", PriorityLowBg, PriorityLowText, selected, onSelect)
        }
    }
}

@Composable
private fun PriorityOption(
    value: Priority,
    label: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    selected: Priority,
    onSelect: (Priority) -> Unit
) {
    val isSelected = selected == value
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) backgroundColor else androidx.compose.ui.graphics.Color(0xFFF1F1F5))
            .clickable { onSelect(value) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) textColor else TextGrey,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall
        )
    }
}