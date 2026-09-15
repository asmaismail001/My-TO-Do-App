package com.example.mytodoapp.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.mytodoapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorFor(isDark))
            .imePadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Accent.copy(alpha = 0.12f))
                    .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📝",
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.welcome_back),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                color = textPrimaryFor(isDark),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.sign_in_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondaryFor(isDark),
                textAlign = TextAlign.Center
            )

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

            OutlinedTextField(
                value = viewModel.loginEmail,
                onValueChange = {
                    viewModel.loginEmail = it
                    viewModel.clearMessages()
                },
                label = { Text(stringResource(R.string.email_address)) },
                placeholder = { Text("yourname@example.com") },
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
                value = viewModel.loginPassword,
                onValueChange = {
                    viewModel.loginPassword = it
                    viewModel.clearMessages()
                },
                label = { Text(stringResource(R.string.password)) },
                placeholder = { Text("••••••••") },
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

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.login(onLoginSuccess) },
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
                        text = stringResource(R.string.login),
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
                    text = stringResource(R.string.dont_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryFor(isDark)
                )
                Text(
                    text = stringResource(R.string.sign_up),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Accent,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        onNavigateToSignup()
                    }
                )
            }
        }
    }
}
