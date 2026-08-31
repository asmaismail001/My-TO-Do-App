package com.example.mytodoapp.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.ui.*
import com.example.mytodoapp.ui.components.rememberBitmapFromUri
import com.example.mytodoapp.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = surfaceColorFor(isDark),
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDark)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out?",
                    color = textSecondaryFor(isDark)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogoutSuccess)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed, contentColor = Color.White)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = textSecondaryFor(isDark))
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = backgroundColorFor(isDark),
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textPrimaryFor(isDark))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColorFor(isDark),
                    titleContentColor = textPrimaryFor(isDark)
                )
            )
        }
    ) { paddingValues ->
        if (viewModel.isLoading && viewModel.profileData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Accent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading profile...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryFor(isDark)
                    )
                }
            }
        } else {
            val user = viewModel.profileData
            if (user == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = viewModel.errorMessage ?: "Unable to load profile.",
                            color = DeleteRed,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = Accent)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val profileBitmap = rememberBitmapFromUri(user.profileImage)

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Accent.copy(alpha = 0.12f))
                            .border(2.dp, Accent.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = user.name.firstOrNull()?.toString()?.uppercase() ?: "👤",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Accent,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = textPrimaryFor(isDark),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryFor(isDark),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                        border = BorderStroke(1.dp, cardBorderColorFor(isDark))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = textPrimaryFor(isDark)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            InfoRow(
                                icon = Icons.Default.Person,
                                label = "Full Name",
                                value = user.name,
                                isDark = isDark
                            )
                            HorizontalDivider(
                                color = cardBorderColorFor(isDark).copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            InfoRow(
                                icon = Icons.Default.Email,
                                label = "Email Address",
                                value = user.email,
                                isDark = isDark
                            )
                            HorizontalDivider(
                                color = cardBorderColorFor(isDark).copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            InfoRow(
                                icon = Icons.Default.Phone,
                                label = "Phone Number",
                                value = user.phone ?: "Not Provided",
                                isDark = isDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileOptionItem(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile",
                        isDark = isDark,
                        onClick = {
                            viewModel.initializeEditFields()
                            onNavigateToEditProfile()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ProfileOptionItem(
                        icon = Icons.Default.Logout,
                        title = "Log Out",
                        textColor = DeleteRed,
                        iconColor = DeleteRed,
                        isDark = isDark,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Accent.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = textMutedFor(isDark)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textPrimaryFor(isDark)
            )
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isDark: Boolean,
    textColor: Color = textPrimaryFor(isDark),
    iconColor: Color = Accent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
        border = BorderStroke(1.dp, cardBorderColorFor(isDark))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textMutedFor(isDark),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
