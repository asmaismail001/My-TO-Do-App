package com.example.mytodoapp.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Color
import com.example.mytodoapp.R
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.theme.MyTODoAppTheme
import com.example.mytodoapp.util.LocaleHelper
import com.example.mytodoapp.util.PreferencesManager

class SnoozeActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesManager(newBase)
        val lang = prefs.getLanguage()
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskId = intent.getIntExtra("taskId", 0)
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task"
        val dueTimeMillis = intent.getLongExtra("dueTimeMillis", 0L)
        val endTimeMillis = intent.getLongExtra("endTimeMillis", 0L)

        val prefs = PreferencesManager(applicationContext)
        val isDark = prefs.isDarkTheme()
        val lang = prefs.getLanguage()

        setContent {
            val activity = this@SnoozeActivity
            val localizedContext = remember(lang) { LocaleHelper.setLocale(activity, lang) }
            val layoutDirection = remember(lang) { LocaleHelper.getLayoutDirection(lang) }
            val config = remember(localizedContext) { localizedContext.resources.configuration }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides config,
                LocalLayoutDirection provides layoutDirection,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides activity
            ) {
                MyTODoAppTheme(dynamicColor = false) {
                    SnoozeDialogContent(
                        taskTitle = taskTitle,
                        isDark = isDark,
                        onDismiss = { finish() },
                        onSnoozeSelected = { minutes ->
                            val dueVal = if (dueTimeMillis > 0) dueTimeMillis else null
                            val endVal = if (endTimeMillis > 0) endTimeMillis else null
                            NotificationScheduler.scheduleSnooze(
                                context = applicationContext,
                                taskId = taskId,
                                taskTitle = taskTitle,
                                dueTimeMillis = dueVal,
                                endTimeMillis = endVal,
                                snoozeMinutes = minutes
                            )
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(taskId)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SnoozeDialogContent(
    taskTitle: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSnoozeSelected: (Int) -> Unit
) {
    var isCustomSelected by remember { mutableStateOf(false) }
    var customMinutesStr by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) com.example.mytodoapp.ui.SurfaceColorDark else com.example.mytodoapp.ui.SurfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.35f)),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.snooze_reminder),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDark) com.example.mytodoapp.ui.TextPrimaryDark else com.example.mytodoapp.ui.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (!isCustomSelected) {
                    Text(
                        text = stringResource(R.string.snooze_for, taskTitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) com.example.mytodoapp.ui.TextSecondaryDark else com.example.mytodoapp.ui.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val options = listOf(
                        5 to stringResource(R.string.snooze_5_min),
                        10 to stringResource(R.string.snooze_10_min),
                        15 to stringResource(R.string.snooze_15_min),
                        30 to stringResource(R.string.snooze_30_min),
                        60 to stringResource(R.string.snooze_1_hour)
                    )

                    options.forEach { (minutes, label) ->
                        TextButton(
                            onClick = { onSnoozeSelected(minutes) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                text = label,
                                color = Accent,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Accent.copy(alpha = 0.15f)))
                    }

                    TextButton(
                        onClick = { isCustomSelected = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.custom),
                            color = Accent,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Accent.copy(alpha = 0.15f)))

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = stringResource(R.string.cancel),
                                color = if (isDark) com.example.mytodoapp.ui.TextSecondaryDark else com.example.mytodoapp.ui.TextSecondary
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.enter_custom_snooze),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) com.example.mytodoapp.ui.TextSecondaryDark else com.example.mytodoapp.ui.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = customMinutesStr,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                customMinutesStr = newValue
                            }
                        },
                        label = { Text(stringResource(R.string.custom_minutes)) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Accent.copy(alpha = 0.35f),
                            focusedLabelColor = Accent,
                            unfocusedLabelColor = if (isDark) com.example.mytodoapp.ui.TextSecondaryDark else com.example.mytodoapp.ui.TextSecondary,
                            focusedTextColor = if (isDark) com.example.mytodoapp.ui.TextPrimaryDark else com.example.mytodoapp.ui.TextPrimary,
                            unfocusedTextColor = if (isDark) com.example.mytodoapp.ui.TextPrimaryDark else com.example.mytodoapp.ui.TextPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isCustomSelected = false }) {
                            Text(
                                text = stringResource(R.string.back),
                                color = if (isDark) com.example.mytodoapp.ui.TextSecondaryDark else com.example.mytodoapp.ui.TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val mins = customMinutesStr.toIntOrNull() ?: 5
                                if (mins > 0) {
                                    onSnoozeSelected(mins)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent)
                        ) {
                            Text(stringResource(R.string.snooze), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

