package com.example.mytodoapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.Screen
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor

@Composable
fun BottomNavBar(selected: Screen, onSelect: (Screen) -> Unit) {
    val isDark = LocalIsDarkTheme.current
    val borderColor = cardBorderColorFor(isDark).copy(alpha = 0.5f)

    NavigationBar(
        containerColor = surfaceColorFor(isDark),
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
            drawLine(
                color = borderColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        NavigationBarItem(
            selected = selected == Screen.ALL,
            onClick = { onSelect(Screen.ALL) },
            icon = {
                Icon(
                    imageVector = if (selected == Screen.ALL) Icons.Filled.List else Icons.Outlined.List,
                    contentDescription = "All Tasks"
                )
            },
            label = { Text("All") },
            colors = navColors(isDark)
        )
        NavigationBarItem(
            selected = selected == Screen.COMPLETED,
            onClick = { onSelect(Screen.COMPLETED) },
            icon = {
                Icon(
                    imageVector = if (selected == Screen.COMPLETED) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Completed"
                )
            },
            label = { Text("Completed") },
            colors = navColors(isDark)
        )
        NavigationBarItem(
            selected = selected == Screen.PENDING,
            onClick = { onSelect(Screen.PENDING) },
            icon = {
                Icon(
                    imageVector = if (selected == Screen.PENDING) Icons.Filled.HourglassEmpty else Icons.Outlined.HourglassEmpty,
                    contentDescription = "Pending"
                )
            },
            label = { Text("Pending") },
            colors = navColors(isDark)
        )
        NavigationBarItem(
            selected = selected == Screen.CALENDAR,
            onClick = { onSelect(Screen.CALENDAR) },
            icon = {
                Icon(
                    imageVector = if (selected == Screen.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Calendar"
                )
            },
            label = { Text("Calendar") },
            colors = navColors(isDark)
        )
    }
}

@Composable
private fun navColors(isDark: Boolean) = NavigationBarItemDefaults.colors(
    selectedIconColor = Accent,
    selectedTextColor = Accent,
    unselectedIconColor = textMutedFor(isDark),
    unselectedTextColor = textMutedFor(isDark),
    indicatorColor = Accent.copy(alpha = 0.08f)
)