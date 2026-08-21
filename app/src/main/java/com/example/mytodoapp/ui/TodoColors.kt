package com.example.mytodoapp.ui

import androidx.compose.ui.graphics.Color

// The one bright accent — used sparingly and consistently
val Accent = Color(0xFF0A8585)
val AccentLight = Color(0xFFE6F3F3)

// Light theme
val BackgroundColor = Color(0xFFF4F6F6)
val SurfaceColor = Color(0xFFFFFFFF)
val SearchBarBackground = Color(0xFFEEF0F3)
val DialogFieldBackground = Color(0xFFF3F4F6)
val CardBorderColor = Color(0xFFE5E7EB)

val TextPrimary = Color(0xFF111827)
val TextSecondary = Color(0xFF4B5563)
val TextMuted = Color(0xFF9CA3AF)

// Dark theme
val BackgroundColorDark = Color(0xFF0E1516)
val SurfaceColorDark = Color(0xFF152022)
val SearchBarBackgroundDark = Color(0xFF222836)
val DialogFieldBackgroundDark = Color(0xFF222836)
val CardBorderColorDark = Color(0xFF2E3545)

val TextPrimaryDark = Color(0xFFF9FAFB)
val TextSecondaryDark = Color(0xFFD1D5DB)
val TextMutedDark = Color(0xFF6B7280)

val DeleteRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981) // completed uses modern green if needed, or accent

// Backward-compatible aliases so older files keep compiling
val AccentPurple = Accent
val AccentPurpleLight = AccentLight
val SalmonPink = Accent
val SageGreen = Accent
val PrimaryPurple = Accent
val PrimaryIndigo = Accent
val CardCompleted = AccentLight
val CardPending = SurfaceColor
val TextDark = TextPrimary
val TextGrey = TextSecondary
val PriorityHigh = Color(0xFFEF4444)
val PriorityMedium = Color(0xFFF59E0B)
val PriorityLow = Color(0xFF94A3B8)
val PriorityHighBg = Color(0xFFFEE2E2)
val PriorityHighText = Color(0xFFEF4444)
val PriorityMediumBg = Color(0xFFFEF3C7)
val PriorityMediumText = Color(0xFFB45309)
val PriorityLowBg = Color(0xFFE2E8F0)
val PriorityLowText = Color(0xFF64748B)

fun backgroundColorFor(isDark: Boolean) = if (isDark) BackgroundColorDark else BackgroundColor
fun surfaceColorFor(isDark: Boolean) = if (isDark) SurfaceColorDark else SurfaceColor
fun textPrimaryFor(isDark: Boolean) = if (isDark) TextPrimaryDark else TextPrimary
fun textSecondaryFor(isDark: Boolean) = if (isDark) TextSecondaryDark else TextSecondary
fun textMutedFor(isDark: Boolean) = if (isDark) TextMutedDark else TextMuted
fun searchBarBackgroundFor(isDark: Boolean) = if (isDark) SearchBarBackgroundDark else SearchBarBackground
fun dialogFieldBackgroundFor(isDark: Boolean) = if (isDark) DialogFieldBackgroundDark else DialogFieldBackground
fun cardBorderColorFor(isDark: Boolean) = if (isDark) CardBorderColorDark else CardBorderColor