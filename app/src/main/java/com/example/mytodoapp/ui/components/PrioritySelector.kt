package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor

@Composable
fun PrioritySelector(
    selected: Priority,
    onSelect: (Priority) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Priority",
            color = textSecondaryFor(isDark),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF222836) else Color(0xFFF3F4F6))
                .border(
                    width = 1.dp,
                    color = cardBorderColorFor(isDark),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityDot(priority = selected)
                    Text(
                        text = when (selected) {
                            Priority.HIGH -> "High"
                            Priority.MEDIUM -> "Medium"
                            Priority.LOW -> "Low"
                        },
                        color = textPrimaryFor(isDark),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Indicator",
                    tint = textSecondaryFor(isDark)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .background(surfaceColorFor(isDark))
                    .border(1.dp, cardBorderColorFor(isDark), RoundedCornerShape(12.dp))
            ) {
                Priority.values().forEach { priority ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PriorityDot(priority = priority)
                                Text(
                                    text = when (priority) {
                                        Priority.HIGH -> "High"
                                        Priority.MEDIUM -> "Medium"
                                        Priority.LOW -> "Low"
                                    },
                                    color = textPrimaryFor(isDark),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        onClick = {
                            onSelect(priority)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = textPrimaryFor(isDark)
                        )
                    )
                }
            }
        }
    }
}