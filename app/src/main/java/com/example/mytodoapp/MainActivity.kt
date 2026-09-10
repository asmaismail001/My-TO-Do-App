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
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytodoapp.repository.AuthRepository
import com.example.mytodoapp.repository.TodoRepository
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.SplashContent
import com.example.mytodoapp.ui.TodoScreen
import com.example.mytodoapp.ui.auth.BiometricLockScreen
import com.example.mytodoapp.util.BiometricAuthenticator
import com.example.mytodoapp.util.BiometricResult
import com.example.mytodoapp.util.LocaleHelper
import com.example.mytodoapp.util.PreferencesManager
import com.example.mytodoapp.util.SessionManager
import com.example.mytodoapp.viewmodel.TodoViewModel
import com.example.mytodoapp.viewmodel.TodoViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val pendingOpenTaskId = mutableStateOf<Int?>(null)
    private val isAppLocked = mutableStateOf(false)
    private val lockErrorMessage = mutableStateOf<String?>(null)
    private var isPromptCurrentlyActive = false

    private val sessionManager by lazy { SessionManager(applicationContext) }

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            // App entered background or screen locked
            if (sessionManager.isLoggedIn() && sessionManager.isBiometricLockEnabled()) {
                sessionManager.saveLastBackgroundTime(System.currentTimeMillis())
            }
        }

        override fun onStart(owner: LifecycleOwner) {
            // App returned to foreground
            checkAndHandleAppLock()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesManager(newBase)
        val lang = prefs.getLanguage()
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

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

        // Register process lifecycle observer for app background/foreground transitions
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // Cold-start lock check
        checkAndHandleAppLock()

        setContent {
            val activity = this@MainActivity
            val prefs = remember { PreferencesManager(activity) }
            var currentLanguage by remember { mutableStateOf(prefs.getLanguage()) }
            val localizedContext = remember(currentLanguage) {
                LocaleHelper.setLocale(activity, currentLanguage)
            }
            val layoutDirection = remember(currentLanguage) {
                LocaleHelper.getLayoutDirection(currentLanguage)
            }

            var themeMode by remember { mutableStateOf(prefs.getThemeMode()) }
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = remember(themeMode, systemInDark) {
                when (themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> systemInDark
                }
            }
            val taskIdToOpen by pendingOpenTaskId
            val locked by isAppLocked
            val errorMsg by lockErrorMessage

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalLayoutDirection provides layoutDirection,
                LocalIsDarkTheme provides isDarkTheme,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides activity
            ) {
                com.example.mytodoapp.ui.theme.MyTODoAppTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (locked) {
                            BiometricLockScreen(
                                onAuthenticateClick = { triggerBiometricPrompt() },
                                onLogoutClick = { performLockedLogout() },
                                errorMessage = errorMsg,
                                isDark = isDarkTheme
                            )
                        } else {
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
                                val authRepository = AuthRepository(applicationContext)
                                val profileRepository = com.example.mytodoapp.repository.ProfileRepository(applicationContext)
                                val weatherRepository = com.example.mytodoapp.repository.WeatherRepository()

                                val viewModel: TodoViewModel = viewModel(
                                    factory = TodoViewModelFactory(repository, applicationContext)
                                )
                                val authViewModel: com.example.mytodoapp.viewmodel.AuthViewModel = viewModel(
                                    factory = com.example.mytodoapp.viewmodel.AuthViewModelFactory(authRepository, repository)
                                )
                                val profileViewModel: com.example.mytodoapp.viewmodel.ProfileViewModel = viewModel(
                                    factory = com.example.mytodoapp.viewmodel.ProfileViewModelFactory(authRepository, profileRepository)
                                )
                                val weatherViewModel: com.example.mytodoapp.viewmodel.WeatherViewModel = viewModel(
                                    factory = com.example.mytodoapp.viewmodel.WeatherViewModelFactory(weatherRepository, applicationContext)
                                )

                                TodoScreen(
                                    viewModel = viewModel,
                                    authViewModel = authViewModel,
                                    profileViewModel = profileViewModel,
                                    weatherViewModel = weatherViewModel,
                                    openTaskId = taskIdToOpen,
                                    onOpenTaskConsumed = { pendingOpenTaskId.value = null },
                                    themeMode = themeMode,
                                    onThemeModeChange = { mode ->
                                        themeMode = mode
                                        prefs.setThemeMode(mode)
                                    },
                                    currentLanguage = currentLanguage,
                                    onLanguageChange = { lang ->
                                        currentLanguage = lang
                                        prefs.setLanguage(lang)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndHandleAppLock() {
        if (sessionManager.isLoggedIn() && sessionManager.isBiometricLockEnabled()) {
            val lastBgTime = sessionManager.getLastBackgroundTime()
            if (lastBgTime > 0L) {
                val elapsed = System.currentTimeMillis() - lastBgTime
                if (elapsed >= BACKGROUND_TIMEOUT_MILLIS) {
                    isAppLocked.value = true
                    triggerBiometricPrompt()
                }
            }
        }
    }

    private fun triggerBiometricPrompt() {
        if (!sessionManager.isLoggedIn() || !sessionManager.isBiometricLockEnabled()) {
            isAppLocked.value = false
            return
        }
        if (isPromptCurrentlyActive) return
        isPromptCurrentlyActive = true
        lockErrorMessage.value = null

        BiometricAuthenticator.authenticate(
            activity = this,
            title = getString(R.string.app_locked),
            subtitle = getString(R.string.app_locked_desc),
            negativeButtonText = getString(R.string.cancel)
        ) { result ->
            isPromptCurrentlyActive = false
            when (result) {
                is BiometricResult.Success -> {
                    isAppLocked.value = false
                    lockErrorMessage.value = null
                    sessionManager.clearLastBackgroundTime()
                }
                is BiometricResult.Cancelled -> {
                    // User dismissed or cancelled: remain locked
                    lockErrorMessage.value = null
                }
                is BiometricResult.Failed -> {
                    lockErrorMessage.value = getString(R.string.biometric_auth_failed)
                }
                is BiometricResult.Error -> {
                    // Only show if not cancelled
                    if (result.errorCode != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED &&
                        result.errorCode != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        result.errorCode != androidx.biometric.BiometricPrompt.ERROR_CANCELED
                    ) {
                        lockErrorMessage.value = result.message
                    }
                }
            }
        }
    }

    private fun performLockedLogout() {
        val authRepository = AuthRepository(applicationContext)
        lifecycleScope.launch {
            authRepository.logout()
            isAppLocked.value = false
            lockErrorMessage.value = null
            isPromptCurrentlyActive = false
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
        const val BACKGROUND_TIMEOUT_MILLIS = 15 * 60 * 1000L // 15 minutes = 900,000 ms

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
