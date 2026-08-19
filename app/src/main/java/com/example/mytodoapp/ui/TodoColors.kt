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

val TaskColorPalette = listOf(
    TaskColorPair(Color(0xFFFDF2EF), Color(0xFFF7DDD5)), // soft coral
    TaskColorPair(Color(0xFFF0F7ED), Color(0xFFDCEDD3)), // soft green
    TaskColorPair(Color(0xFFFDF7E9), Color(0xFFF7EAC4)), // soft gold
    TaskColorPair(Color(0xFFF4F0FB), Color(0xFFE3D9F5)), // soft lavender
    TaskColorPair(Color(0xFFEDF7F8), Color(0xFFD4ECEE)), // soft teal
    TaskColorPair(Color(0xFFFCF0F4), Color(0xFFF6DCE5)), // soft pink
    TaskColorPair(Color(0xFFEEF5FC), Color(0xFFD9E9FA)), // soft sky blue
    TaskColorPair(Color(0xFFFBF2E9), Color(0xFFF3E1CB))  // soft peach
)

fun taskColorFor(id: Int, completed: Boolean): Color {
    val pair = TaskColorPalette[Math.abs(id) % TaskColorPalette.size]
    return if (completed) pair.dark else pair.light
}