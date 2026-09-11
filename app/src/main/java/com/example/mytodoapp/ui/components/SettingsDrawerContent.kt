package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.Screen
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor

@Composable
fun SettingsDrawerContent(
    currentScreen: Screen,
    onScreenSelect: (Screen) -> Unit,
    isDarkTheme: Boolean,
    onLogoutClick: () -> Unit
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
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.menu),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimaryFor(isDarkTheme)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Section
            Text(
                text = stringResource(R.string.navigation).uppercase(),
                fontWeight = FontWeight.Bold,
                color = textMutedFor(isDarkTheme),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            CompactNavigationItem(
                label = stringResource(R.string.dashboard),
                icon = Icons.Outlined.Dashboard,
                isSelected = currentScreen == Screen.DASHBOARD,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.DASHBOARD) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = stringResource(R.string.all_tasks),
                icon = Icons.AutoMirrored.Outlined.List,
                isSelected = currentScreen == Screen.ALL,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.ALL) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = stringResource(R.string.completed_tasks),
                icon = Icons.Outlined.CheckCircle,
                isSelected = currentScreen == Screen.COMPLETED,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.COMPLETED) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = stringResource(R.string.pending_tasks),
                icon = Icons.Outlined.HourglassEmpty,
                isSelected = currentScreen == Screen.PENDING,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.PENDING) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = stringResource(R.string.calendar),
                icon = Icons.Outlined.CalendarMonth,
                isSelected = currentScreen == Screen.CALENDAR,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.CALENDAR) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = stringResource(R.string.my_profile),
                icon = Icons.Outlined.Person,
                isSelected = currentScreen == Screen.PROFILE || currentScreen == Screen.EDIT_PROFILE,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.PROFILE) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactNavigationItem(
                label = stringResource(R.string.settings),
                icon = Icons.Outlined.Settings,
                isSelected = currentScreen == Screen.SETTINGS,
                isDarkTheme = isDarkTheme,
                onClick = { onScreenSelect(Screen.SETTINGS) }
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = cardBorderColorFor(isDarkTheme).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // Account Section
            Text(
                text = stringResource(R.string.account).uppercase(),
                fontWeight = FontWeight.Bold,
                color = textMutedFor(isDarkTheme),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            CompactDrawerItem(
                label = stringResource(R.string.logout),
                icon = Icons.AutoMirrored.Outlined.Logout,
                isDarkTheme = isDarkTheme,
                onClick = onLogoutClick
            )
        }
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