package com.example.mytodoapp.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.ui.*
import com.example.mytodoapp.ui.components.rememberBitmapFromUri
import com.example.mytodoapp.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.editProfileImageUri = uri.toString()
        }
    }

    val selectedImageBitmap = rememberBitmapFromUri(viewModel.editProfileImageUri)

    Scaffold(
        containerColor = backgroundColorFor(isDark),
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.12f))
                    .border(2.dp, Accent.copy(alpha = 0.4f), CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageBitmap != null) {
                    Image(
                        bitmap = selectedImageBitmap,
                        contentDescription = "Selected Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = viewModel.editName.firstOrNull()?.toString()?.uppercase() ?: "👤",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Accent)
                        .border(2.dp, backgroundColorFor(isDark), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Choose Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            viewModel.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeleteRed.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, DeleteRed.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = error,
                        color = DeleteRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            viewModel.successMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = msg,
                        color = SuccessGreen,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            OutlinedTextField(
                value = viewModel.editName,
                onValueChange = {
                    viewModel.editName = it
                    viewModel.clearMessages()
                },
                label = { Text("Full Name") },
                placeholder = { Text("Your Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = textMutedFor(isDark)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = cardBorderColorFor(isDark),
                    focusedContainerColor = surfaceColorFor(isDark),
                    unfocusedContainerColor = surfaceColorFor(isDark),
                    focusedLabelColor = Accent,
                    unfocusedLabelColor = textMutedFor(isDark),
                    focusedTextColor = textPrimaryFor(isDark),
                    unfocusedTextColor = textPrimaryFor(isDark)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.profileData?.email ?: "",
                onValueChange = {},
                label = { Text("Email Address") },
                readOnly = true,
                enabled = false,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = textMutedFor(isDark).copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = cardBorderColorFor(isDark).copy(alpha = 0.7f),
                    disabledContainerColor = if (isDark) Color(0xFF181818) else Color(0xFFF3F4F6),
                    disabledLabelColor = textMutedFor(isDark).copy(alpha = 0.8f),
                    disabledTextColor = textSecondaryFor(isDark).copy(alpha = 0.8f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.editPhone,
                onValueChange = {
                    viewModel.editPhone = it
                    viewModel.clearMessages()
                },
                label = { Text("Phone Number") },
                placeholder = { Text("+1 (234) 567-890") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = textMutedFor(isDark)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = cardBorderColorFor(isDark),
                    focusedContainerColor = surfaceColorFor(isDark),
                    unfocusedContainerColor = surfaceColorFor(isDark),
                    focusedLabelColor = Accent,
                    unfocusedLabelColor = textMutedFor(isDark),
                    focusedTextColor = textPrimaryFor(isDark),
                    unfocusedTextColor = textPrimaryFor(isDark)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    viewModel.updateProfile {
                        Toast.makeText(context, "Changes Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.White),
                enabled = !viewModel.isSaving
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
