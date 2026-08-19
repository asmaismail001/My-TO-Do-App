package com.example.mytodoapp.ui

import androidx.compose.ui.graphics.Color

val BackgroundColor = Color(0xFFFFFFFF)
val SurfaceColor = Color(0xFFFFFFFF)
val SearchBarBackground = Color(0xFFF3E8E3)
val DialogFieldBackground = Color(0xFFF6EEEA)

val AccentPurple = Color(0xFF6B7F66)
val AccentPurpleLight = Color(0xFFE7ECE4)

val SalmonPink = Color(0xFFE8A79B)
val SalmonPinkLight = Color(0xFFFBE7E2)
val SageGreen = Color(0xFF6B7F66)
val SageGreenLight = Color(0xFFE7ECE4)

val TextPrimary = Color(0xFF3A342F)
val TextSecondary = Color(0xFF938A82)
val TextMuted = Color(0xFFB8AFA7)

val SuccessGreen = Color(0xFF6B7F66)
val DeleteRed = Color(0xFFD98A7E)

val PriorityHigh = Color(0xFFD98A7E)
val PriorityMedium = Color(0xFFE0B26B)
val PriorityLow = Color(0xFF6B7F66)

val PrimaryPurple = AccentPurple
val CardCompleted = SageGreenLight
val CardPending = SurfaceColor
val TextDark = TextPrimary
val TextGrey = TextSecondary
val PriorityHighBg = Color(0xFFFBE7E2)
val PriorityHighText = PriorityHigh
val PriorityMediumBg = Color(0xFFFBF1DF)
val PriorityMediumText = PriorityMedium
val PriorityLowBg = SageGreenLight
val PriorityLowText = PriorityLow

data class TaskColorPair(val light: Color, val dark: Color)

// Alternates between sage green and salmon pink, matching the app's theme
val TaskColorPalette = listOf(
    TaskColorPair(Color(0xFFEAF1E7), Color(0xFFCFE0C9)), // sage green
    TaskColorPair(Color(0xFFFBEAE6), Color(0xFFF2CFC5))  // salmon pink
)

fun taskColorFor(id: Int, completed: Boolean): Color {
    val pair = TaskColorPalette[Math.abs(id) % TaskColorPalette.size]
    return if (completed) pair.dark else pair.light
}