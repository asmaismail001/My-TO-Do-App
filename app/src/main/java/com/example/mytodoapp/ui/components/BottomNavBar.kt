package com.example.mytodoapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.mytodoapp.ui.Screen
import com.example.mytodoapp.ui.SageGreen
import com.example.mytodoapp.ui.SurfaceColor
import com.example.mytodoapp.ui.TextMuted

@Composable
fun BottomNavBar(
    selected: Screen,
    onSelect: (Screen) -> Unit
) {
    NavigationBar(containerColor = SurfaceColor) {
        NavigationBarItem(
            selected = selected == Screen.ALL,
            onClick = { onSelect(Screen.ALL) },
            icon = { Icon(Icons.Filled.List, contentDescription = "All Tasks") },
            label = { Text("All") },
            colors = navColors()
        )
        NavigationBarItem(
            selected = selected == Screen.COMPLETED,
            onClick = { onSelect(Screen.COMPLETED) },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Completed") },
            label = { Text("Completed") },
            colors = navColors()
        )
        NavigationBarItem(
            selected = selected == Screen.DUE,
            onClick = { onSelect(Screen.DUE) },
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Due") },
            label = { Text("Due") },
            colors = navColors()
        )
        NavigationBarItem(
            selected = selected == Screen.CALENDAR,
            onClick = { onSelect(Screen.CALENDAR) },
            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Calendar") },
            label = { Text("Calendar") },
            colors = navColors()
        )
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SageGreen,
    selectedTextColor = SageGreen,
    unselectedIconColor = TextMuted,
    unselectedTextColor = TextMuted,
    indicatorColor = SageGreen.copy(alpha = 0.15f)
)