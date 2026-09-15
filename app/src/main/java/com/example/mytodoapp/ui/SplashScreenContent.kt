package com.example.mytodoapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashContent(
    isDark: Boolean = LocalIsDarkTheme.current,
    onAnimationFinished: (() -> Unit)? = null
) {
    // Animation states
    val logoScale = remember { Animatable(0.78f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(18f) }
    val taglineAlpha = remember { Animatable(0f) }
    val footerAlpha = remember { Animatable(0f) }

    // Subtle ambient glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "ambientPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        // Step 1: Logo enters with smooth scale + fade
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
        }

        // Step 2: App Title slides up and fades in
        delay(140)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }

        // Step 3: Tagline and footer fade in
        delay(120)
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
        launch {
            footerAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }

        // Finish splash after ~1250ms total for a crisp, snappy experience
        delay(650)
        onAnimationFinished?.invoke()
    }

    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F171E), // Soft dark navy/teal
                Color(0xFF131D27),
                Color(0xFF0C141C)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF6FAF9), // Soft crisp pale mint/white
                Color(0xFFF1F6F5),
                Color(0xFFE9F2F0)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        // Ambient soft background glow behind logo
        Box(
            modifier = Modifier
                .size(170.dp)
                .scale(pulseScale)
                .alpha(if (isDark) 0.18f else 0.12f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Accent, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Main Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // App Logo Emblem
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .shadow(
                        elevation = if (isDark) 16.dp else 12.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Accent.copy(alpha = 0.4f),
                        ambientColor = Accent.copy(alpha = 0.2f)
                    )
                    .size(92.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0D9488), // Rich Turquoise Teal
                                Color(0xFF0A8585), // Core Brand Accent
                                Color(0xFF065F58)  // Deep Forest Teal
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(200f, 200f)
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.35f else 0.5f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                TaskMasterLogoGraphic(modifier = Modifier.size(54.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name with refined typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffsetY.value.dp)
                    .alpha(textAlpha.value)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.ExtraBold,
                                color = Accent
                            )
                        ) {
                            append("Task")
                        }
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryFor(isDark)
                            )
                        ) {
                            append("Manager")
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline: Organize • Focus • Complete
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textSecondaryFor(isDark),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.alpha(taglineAlpha.value)
                )
            }
        }

        // Bottom Subtle Progress Indicator & Brand Mark
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
                .alpha(footerAlpha.value),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Accent.copy(alpha = 0.35f))
                )
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Accent)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Accent.copy(alpha = 0.35f))
                )
            }
        }
    }
}

/**
 * Modern, clean task manager vector icon with a sleek checkmark,
 * task bullet list, and a vibrant highlight dot.
 */
@Composable
private fun TaskMasterLogoGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background subtle clip pad shape (task list card silhouette)
        val cardRect = RoundRect(
            left = w * 0.14f,
            top = h * 0.16f,
            right = w * 0.86f,
            bottom = h * 0.84f,
            cornerRadius = CornerRadius(w * 0.16f, h * 0.16f)
        )
        val cardPath = Path().apply { addRoundRect(cardRect) }
        drawPath(
            path = cardPath,
            color = Color.White.copy(alpha = 0.12f)
        )

        // Precision Checkmark
        val checkPath = Path().apply {
            moveTo(w * 0.28f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.66f)
            lineTo(w * 0.74f, h * 0.34f)
        }

        // Draw outer glow / shadow for the checkmark
        drawPath(
            path = checkPath,
            color = Color(0xFF065F58).copy(alpha = 0.4f),
            style = Stroke(
                width = w * 0.15f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw crisp checkmark
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(
                width = w * 0.13f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Top-right dynamic focus spark
        drawCircle(
            color = Color(0xFF2DD4BF), // Glowing Mint Accent
            radius = w * 0.08f,
            center = Offset(w * 0.76f, h * 0.28f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.04f,
            center = Offset(w * 0.76f, h * 0.28f)
        )
    }
}
