package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
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
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimaryFor(isDarkTheme)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                SettingRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Sleek dark interface",
                    isDarkTheme = isDarkTheme
                ) {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onDarkThemeChange,
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
            .background(if (isDarkTheme) Color(0xFF1F2633) else Color(0xFFF3F4F6))
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
            .background(if (isDarkTheme) Color(0xFF1F2633).copy(alpha = 0.5f) else Color(0xFFF3F4F6).copy(alpha = 0.5f))
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
