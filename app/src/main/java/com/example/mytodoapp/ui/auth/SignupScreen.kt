package com.example.mytodoapp.ui.auth

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
import com.example.mytodoapp.ui.*
import com.example.mytodoapp.ui.components.rememberBitmapFromUri
import com.example.mytodoapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.signupProfileImageUri = uri.toString()
        }
    }

    val selectedImageBitmap = rememberBitmapFromUri(viewModel.signupProfileImageUri)

    Scaffold(
        containerColor = backgroundColorFor(isDark),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_account), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = textPrimaryFor(isDark))
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
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.12f))
                    .border(2.dp, Accent.copy(alpha = 0.5f), CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageBitmap != null) {
                    Image(
                        bitmap = selectedImageBitmap,
                        contentDescription = stringResource(R.string.select_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = stringResource(R.string.select_photo),
                            tint = Accent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.add_photo),
                            style = MaterialTheme.typography.labelSmall,
                            color = Accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            OutlinedTextField(
                value = viewModel.signupName,
                onValueChange = {
                    viewModel.signupName = it
                    viewModel.clearMessages()
                },
                label = { Text(stringResource(R.string.full_name)) },
                placeholder = { Text("Sarah Connor") },
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
                value = viewModel.signupEmail,
                onValueChange = {
                    viewModel.signupEmail = it
                    viewModel.clearMessages()
                },
                label = { Text(stringResource(R.string.email_address)) },
                placeholder = { Text("sarah@example.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = textMutedFor(isDark)) },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.signupPassword,
                onValueChange = {
                    viewModel.signupPassword = it
                    viewModel.clearMessages()
                },
                label = { Text(stringResource(R.string.password)) },
                placeholder = { Text(stringResource(R.string.password_hint)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = textMutedFor(isDark)) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = null, tint = textMutedFor(isDark))
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.signupConfirmPassword,
                onValueChange = {
                    viewModel.signupConfirmPassword = it
                    viewModel.clearMessages()
                },
                label = { Text(stringResource(R.string.confirm_password)) },
                placeholder = { Text(stringResource(R.string.confirm_password_hint)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = textMutedFor(isDark)) },
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(image, contentDescription = null, tint = textMutedFor(isDark))
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.signup(onSignupSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.White),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sign_up),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryFor(isDark)
                )
                Text(
                    text = stringResource(R.string.login),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Accent,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        onNavigateToLogin()
                    }
                )
            }
        }
    }
}
