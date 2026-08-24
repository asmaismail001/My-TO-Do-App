package com.example.mytodoapp.ui

import android.os.CountDownTimer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.Accent


private const val BREAK_MILLIS = 5 * 60 * 1000L

@Composable
fun FocusTimerDialog(
    todo: Todo,
    onDismiss: () -> Unit,
    onSessionComplete: (Todo) -> Unit
) {
    var isBreak by remember { mutableStateOf(false) }
    var customFocusMinutes by remember { mutableIntStateOf(25) }
    var timeLeftMillis by remember { mutableStateOf(customFocusMinutes * 60 * 1000L) }
    var isRunning by remember { mutableStateOf(false) }
    var timer by remember { mutableStateOf<CountDownTimer?>(null) }

    fun adjustDuration(minutesChange: Int) {
        val nextMinutes = customFocusMinutes + minutesChange
        if (nextMinutes in 5..180) {
            customFocusMinutes = nextMinutes
            timeLeftMillis = nextMinutes * 60 * 1000L
        }
    }

    fun startTimer() {
        timer?.cancel()
        isRunning = true
        timer = object : CountDownTimer(timeLeftMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
            }

            override fun onFinish() {
                timeLeftMillis = 0
                isRunning = false
                if (!isBreak) {
                    onSessionComplete(todo)
                    isBreak = true
                    timeLeftMillis = BREAK_MILLIS
                } else {
                    isBreak = false
                    timeLeftMillis = customFocusMinutes * 60 * 1000L
                }
            }
        }.start()
    }

    fun pauseTimer() {
        timer?.cancel()
        isRunning = false
    }

    fun resetTimer() {
        timer?.cancel()
        isRunning = false
        isBreak = false
        timeLeftMillis = customFocusMinutes * 60 * 1000L
    }

    DisposableEffect(Unit) {
        onDispose { timer?.cancel() }
    }

    val minutes = (timeLeftMillis / 1000) / 60
    val seconds = (timeLeftMillis / 1000) % 60

    AlertDialog(
        onDismissRequest = {
            timer?.cancel()
            onDismiss()
        },
        title = { Text(if (isBreak) "Break time" else "Focusing on: ${todo.title}") },
        text = {
            val isDark = LocalIsDarkTheme.current
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = {
                            val total = if (isBreak) BREAK_MILLIS else (customFocusMinutes * 60 * 1000L)
                            1f - (timeLeftMillis.toFloat() / total.toFloat())
                        },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 6.dp,
                        color = Accent
                    )
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                if (!isBreak && !isRunning && timeLeftMillis == (customFocusMinutes * 60 * 1000L)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { adjustDuration(-5) },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Accent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).width(44.dp)
                        ) {
                            Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Accent)
                        }

                        Text(
                            text = "${customFocusMinutes} min",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryFor(isDark)
                            )
                        )

                        OutlinedButton(
                            onClick = { adjustDuration(5) },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Accent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).width(44.dp)
                        ) {
                            Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Accent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = { if (isRunning) pauseTimer() else startTimer() }) {
                        Icon(
                            if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start"
                        )
                    }
                    IconButton(onClick = { resetTimer() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                timer?.cancel()
                onDismiss()
            }) { Text("Close") }
        }
    )
}