package com.example.mytodoapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
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
        windowInsets = NavigationBarDefaults.windowInsets,
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
            selected = selected == Screen.DASHBOARD,
            onClick = { onSelect(Screen.DASHBOARD) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (selected == Screen.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = stringResource(R.string.dashboard)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.dashboard),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
                )
            },
            colors = navColors(isDark)
        )
        NavigationBarItem(
            selected = selected == Screen.ALL,
            onClick = { onSelect(Screen.ALL) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (selected == Screen.ALL) Icons.Filled.List else Icons.Outlined.List,
                    contentDescription = stringResource(R.string.all_tasks)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.all),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
                )
            },
            colors = navColors(isDark)
        )
        NavigationBarItem(
            selected = selected == Screen.COMPLETED,
            onClick = { onSelect(Screen.COMPLETED) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (selected == Screen.COMPLETED) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(R.string.completed)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.completed),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
                )
            },
            colors = navColors(isDark)
        )
        NavigationBarItem(
            selected = selected == Screen.PENDING,
            onClick = { onSelect(Screen.PENDING) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (selected == Screen.PENDING) Icons.Filled.HourglassEmpty else Icons.Outlined.HourglassEmpty,
                    contentDescription = stringResource(R.string.pending)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.pending),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
                )
            },
            colors = navColors(isDark)
        )
        NavigationBarItem(
            selected = selected == Screen.CALENDAR,
            onClick = { onSelect(Screen.CALENDAR) },
            alwaysShowLabel = true,
            icon = {
                Icon(
                    imageVector = if (selected == Screen.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = stringResource(R.string.calendar)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.calendar),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
                )
            },
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