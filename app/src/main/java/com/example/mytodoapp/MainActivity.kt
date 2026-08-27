package com.example.mytodoapp

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import com.example.mytodoapp.notification.ReminderReceiver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytodoapp.repository.TodoRepository
import com.example.mytodoapp.ui.SplashContent
import com.example.mytodoapp.ui.TodoScreen
import com.example.mytodoapp.viewmodel.TodoViewModel
import com.example.mytodoapp.viewmodel.TodoViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val pendingOpenTaskId = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ReminderReceiver.ensureChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            android.util.Log.d(
                "MainActivity",
                "canScheduleExactAlarms=${alarmManager.canScheduleExactAlarms()}"
            )
        }

        pendingOpenTaskId.value = readTaskId(intent)

        setContent {
            val prefs = remember { com.example.mytodoapp.util.PreferencesManager(applicationContext) }
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = remember(systemInDark) {
                when (prefs.getThemeMode()) {
                    "light" -> false
                    "dark" -> true
                    else -> systemInDark
                }
            }
            val taskIdToOpen by pendingOpenTaskId

            com.example.mytodoapp.ui.theme.MyTODoAppTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashContent()
                        LaunchedEffect(Unit) {
                            delay(1600)
                            showSplash = false
                            if (pendingOpenTaskId.value == null) {
                                requestIgnoreBatteryOptimizations()
                            }
                        }
                    } else {
                        val repository = TodoRepository(applicationContext)
                        val viewModel: TodoViewModel = viewModel(
                            factory = TodoViewModelFactory(repository, applicationContext)
                        )
                        TodoScreen(
                            viewModel = viewModel,
                            openTaskId = taskIdToOpen,
                            onOpenTaskConsumed = { pendingOpenTaskId.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingOpenTaskId.value = readTaskId(intent)
    }

    private fun readTaskId(intent: Intent?): Int? {
        if (intent == null) return null
        val extraId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        if (extraId > 0) return extraId
        return intent.data?.lastPathSegment?.toIntOrNull()?.takeIf { it > 0 }
    }

    private fun requestIgnoreBatteryOptimizations() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Some OEMs block this intent; fail silently, user can still
                // enable it manually from system Settings > Battery.
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "taskId"

        fun taskDetailsIntent(context: Context, taskId: Int): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("todo://task/$taskId")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_TASK_ID, taskId)
            }
        }
    }
}
