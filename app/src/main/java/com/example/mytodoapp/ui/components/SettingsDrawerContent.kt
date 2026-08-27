package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.mytodoapp.ui.Screen
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor

@Composable
fun SettingsDrawerContent(
    currentScreen: Screen,
    onScreenSelect: (Screen) -> Unit,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    isDarkTheme: Boolean,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = surfaceColorFor(isDarkTheme),
        drawerTonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Menu & Settings",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimaryFor(isDarkTheme)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Section
            Text(
                text = "NAVIGATION",
                fontWeight = FontWeight.Bold,
                color = textMutedFor(isDarkTheme),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            CompactNavigationItem(
                label = "Dashboard",
                icon = Icons.Outlined.Dashboard,
                isSelected = currentScreen == Screen.DASHBOARD,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.DASHBOARD) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = "All Tasks",
                icon = Icons.Outlined.List,
                isSelected = currentScreen == Screen.ALL,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.ALL) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = "Completed Tasks",
                icon = Icons.Outlined.CheckCircle,
                isSelected = currentScreen == Screen.COMPLETED,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.COMPLETED) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = "Pending Tasks",
                icon = Icons.Outlined.HourglassEmpty,
                isSelected = currentScreen == Screen.PENDING,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.PENDING) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = "Calendar",
                icon = Icons.Outlined.CalendarMonth,
                isSelected = currentScreen == Screen.CALENDAR,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.CALENDAR) }
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = cardBorderColorFor(isDarkTheme).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(28.dp))

            // Preferences Section
            Text(
                text = "PREFERENCES",
                fontWeight = FontWeight.Bold,
                color = textMutedFor(isDarkTheme),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingItemCard(isDarkTheme = isDarkTheme) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = null,
                        tint = textSecondaryFor(isDarkTheme),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme Mode",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textPrimaryFor(isDarkTheme)
                        )
                        Text(
                            text = when (themeMode) {
                                "light" -> "Light Theme"
                                "dark" -> "Dark Theme"
                                else -> "System Default"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = textMutedFor(isDarkTheme)
                        )
                    }
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(
                            onClick = { dropdownExpanded = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Accent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            val displayText = when (themeMode) {
                                "light" -> "Light ▼"
                                "dark" -> "Dark ▼"
                                else -> "System ▼"
                            }
                            Text(text = displayText, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(surfaceColorFor(isDarkTheme))
                        ) {
                            listOf(
                                "system" to "System Default",
                                "light" to "Light",
                                "dark" to "Dark"
                            ).forEach { (mode, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = textPrimaryFor(isDarkTheme)) },
                                    onClick = {
                                        onThemeModeChange(mode)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingItemCard(isDarkTheme = isDarkTheme) {
                SettingRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    subtitle = "Due date reminders",
                    isDarkTheme = isDarkTheme
                ) {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChange,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Accent,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = textMutedFor(isDarkTheme).copy(alpha = 0.3f),
                            uncheckedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = cardBorderColorFor(isDarkTheme).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(28.dp))

            // Data Management Section
            Text(
                text = "DATA MANAGEMENT",
                fontWeight = FontWeight.Bold,
                color = textMutedFor(isDarkTheme),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            CompactDrawerItem(
                label = "Export Tasks",
                icon = Icons.Outlined.FileUpload,
                isDarkTheme = isDarkTheme,
                onClick = onExportClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactDrawerItem(
                label = "Import Tasks",
                icon = Icons.Outlined.FileDownload,
                isDarkTheme = isDarkTheme,
                onClick = onImportClick
            )
        }
    }
}

@Composable
private fun SettingItemCard(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkTheme) Color(0xFF242424) else Color(0xFFF3F4F6))
            .border(1.dp, cardBorderColorFor(isDarkTheme), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isDarkTheme: Boolean,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textSecondaryFor(isDarkTheme),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                color = textPrimaryFor(isDarkTheme)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textMutedFor(isDarkTheme)
            )
        }
        trailing()
    }
}

@Composable
private fun CompactDrawerItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDarkTheme) Color(0xFF242424).copy(alpha = 0.5f) else Color(0xFFF3F4F6).copy(alpha = 0.5f))
            .border(1.dp, cardBorderColorFor(isDarkTheme).copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textSecondaryFor(isDarkTheme),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondaryFor(isDarkTheme)
        )
    }
}

@Composable
private fun CompactNavigationItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) {
        Accent.copy(alpha = 0.12f)
    } else {
        if (isDarkTheme) Color(0xFF242424).copy(alpha = 0.4f) else Color(0xFFF3F4F6).copy(alpha = 0.4f)
    }
    val borderCol = if (isSelected) {
        Accent.copy(alpha = 0.4f)
    } else {
        cardBorderColorFor(isDarkTheme).copy(alpha = 0.5f)
    }
    val textCol = if (isSelected) Accent else textSecondaryFor(isDarkTheme)
    val iconCol = if (isSelected) Accent else textSecondaryFor(isDarkTheme)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconCol,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = textCol
        )
    }
}
