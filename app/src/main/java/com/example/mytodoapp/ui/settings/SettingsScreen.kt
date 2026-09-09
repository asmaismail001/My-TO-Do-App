package com.example.mytodoapp.ui.settings

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
import com.example.mytodoapp.ui.*
import com.example.mytodoapp.util.LocaleHelper
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    defaultReminderMinutes: Int,
    onDefaultReminderMinutesChange: (Int) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    lastBackupTime: String?,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onBack: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColorFor(isDark),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = textPrimaryFor(isDark)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColorFor(isDark),
                    titleContentColor = textPrimaryFor(isDark)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 1. Appearance / Theme Category
            SettingsCategorySection(
                title = stringResource(R.string.appearance),
                isDark = isDark
            ) {
                AppearanceSettingCard(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    isDark = isDark
                )
            }

            // 2. Notifications Category
            SettingsCategorySection(
                title = stringResource(R.string.notifications),
                isDark = isDark
            ) {
                NotificationSettingCard(
                    notificationsEnabled = notificationsEnabled,
                    onNotificationsEnabledChange = onNotificationsEnabledChange,
                    defaultReminderMinutes = defaultReminderMinutes,
                    onDefaultReminderMinutesChange = onDefaultReminderMinutesChange,
                    isDark = isDark
                )
            }

            // 3. Task Import & Export Category
            SettingsCategorySection(
                title = stringResource(R.string.task_import_export),
                isDark = isDark
            ) {
                ImportExportSettingCard(
                    lastBackupTime = lastBackupTime,
                    onExportClick = onExportClick,
                    onImportClick = onImportClick,
                    isDark = isDark
                )
            }

            // 4. Language Category
            SettingsCategorySection(
                title = stringResource(R.string.language),
                isDark = isDark
            ) {
                LanguageSettingCard(
                    currentLanguage = currentLanguage,
                    onClick = { showLanguageDialog = true },
                    isDark = isDark
                )
            }

            // Version info footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.app_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = textMutedFor(isDark)
                )
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelect = { langCode ->
                onLanguageChange(langCode)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
            isDark = isDark
        )
    }
}

@Composable
private fun SettingsCategorySection(
    title: String,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = Accent,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun AppearanceSettingCard(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
        border = BorderStroke(1.dp, cardBorderColorFor(isDark))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.theme),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textPrimaryFor(isDark)
                    )
                    Text(
                        text = stringResource(R.string.theme_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedFor(isDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Segmented Theme Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF242424) else Color(0xFFF3F4F6))
                    .border(1.dp, cardBorderColorFor(isDark), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ThemeOptionButton(
                    label = stringResource(R.string.theme_system),
                    icon = Icons.Outlined.BrightnessAuto,
                    isSelected = themeMode == "system",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange("system") }
                )
                ThemeOptionButton(
                    label = stringResource(R.string.theme_light),
                    icon = Icons.Outlined.LightMode,
                    isSelected = themeMode == "light",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange("light") }
                )
                ThemeOptionButton(
                    label = stringResource(R.string.theme_dark),
                    icon = Icons.Outlined.DarkMode,
                    isSelected = themeMode == "dark",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { onThemeModeChange("dark") }
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Accent else Color.Transparent
    val textCol = if (isSelected) Color.White else textSecondaryFor(isDark)
    val iconCol = if (isSelected) Color.White else textSecondaryFor(isDark)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 9.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconCol,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.5.sp
            ),
            color = textCol,
            maxLines = 1
        )
    }
}

@Composable
private fun NotificationSettingCard(
    notificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    defaultReminderMinutes: Int,
    onDefaultReminderMinutesChange: (Int) -> Unit,
    isDark: Boolean
) {
    var expandedIntervals by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
        border = BorderStroke(1.dp, cardBorderColorFor(isDark))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Master notification toggle row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.task_reminders),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textPrimaryFor(isDark)
                    )
                    Text(
                        text = stringResource(R.string.manage_reminders),
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedFor(isDark)
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Accent,
                        checkedThumbColor = Color.White,
                        uncheckedTrackColor = textMutedFor(isDark).copy(alpha = 0.3f),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            // Expandable default reminder time row
            AnimatedVisibility(
                visible = notificationsEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = cardBorderColorFor(isDark).copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF242424) else Color(0xFFF3F4F6))
                            .border(1.dp, cardBorderColorFor(isDark), RoundedCornerShape(12.dp))
                            .clickable { expandedIntervals = !expandedIntervals }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.reminder_time),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = textPrimaryFor(isDark)
                            )
                            Text(
                                text = formatReminderInterval(defaultReminderMinutes),
                                style = MaterialTheme.typography.bodySmall,
                                color = Accent,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = if (expandedIntervals) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = textSecondaryFor(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (expandedIntervals) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF242424) else Color(0xFFF3F4F6))
                                .border(1.dp, cardBorderColorFor(isDark), RoundedCornerShape(12.dp))
                                .padding(vertical = 4.dp)
                        ) {
                            listOf(5, 10, 15, 30, 60).forEach { minutes ->
                                val isSelected = defaultReminderMinutes == minutes
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDefaultReminderMinutesChange(minutes)
                                            expandedIntervals = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatReminderInterval(minutes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) Accent else textPrimaryFor(isDark),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun formatReminderInterval(minutes: Int): String {
    return when (minutes) {
        5 -> stringResource(R.string.min_before_5)
        10 -> stringResource(R.string.min_before_10)
        15 -> stringResource(R.string.min_before_15)
        30 -> stringResource(R.string.min_before_30)
        60 -> stringResource(R.string.hour_before_1)
        else -> "$minutes ${stringResource(R.string.minutes_abbr)}"
    }
}

@Composable
private fun ImportExportSettingCard(
    lastBackupTime: String?,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
        border = BorderStroke(1.dp, cardBorderColorFor(isDark))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.task_import_export),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textPrimaryFor(isDark)
                    )
                    Text(
                        text = stringResource(R.string.backup_restore_tasks),
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedFor(isDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Last backup status text
            Text(
                text = if (lastBackupTime != null) {
                    stringResource(R.string.last_backup, lastBackupTime)
                } else {
                    stringResource(R.string.last_backup_none)
                },
                style = MaterialTheme.typography.labelSmall,
                color = textMutedFor(isDark),
                modifier = Modifier.padding(start = 2.dp, bottom = 12.dp)
            )

            // Export & Import Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onExportClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Accent.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.export_tasks),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = onImportClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.import_tasks),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSettingCard(
    currentLanguage: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
        border = BorderStroke(1.dp, cardBorderColorFor(isDark))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textPrimaryFor(isDark)
                )
                Text(
                    text = LocaleHelper.getDisplayName(currentLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = Accent,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = textMutedFor(isDark),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = surfaceColorFor(isDark),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.select_language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textPrimaryFor(isDark)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocaleHelper.SUPPORTED_LANGUAGES.forEach { lang ->
                    val isSelected = currentLanguage == lang.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Accent.copy(alpha = 0.12f)
                                else if (isDark) Color(0xFF242424)
                                else Color(0xFFF3F4F6)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Accent else cardBorderColorFor(isDark),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onLanguageSelect(lang.code) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = lang.nativeName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                ),
                                color = if (isSelected) Accent else textPrimaryFor(isDark)
                            )
                            if (lang.nativeName != lang.englishName) {
                                Text(
                                    text = lang.englishName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textMutedFor(isDark)
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = textSecondaryFor(isDark)
                )
            }
        }
    )
}
