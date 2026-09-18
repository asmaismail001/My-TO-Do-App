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
    // Coordinated animation states
    val logoScale = remember { Animatable(0.84f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(14f) }
    val taglineAlpha = remember { Animatable(0f) }
    val footerAlpha = remember { Animatable(0f) }

    // Ambient soft pulse behind the logo
    val infiniteTransition = rememberInfiniteTransition(label = "ambientGlowPulse")
    val ambientPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientGlowScale"
    )

    LaunchedEffect(Unit) {
        // Step 1: Logo enters with smooth scale + alpha transition
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 440, easing = FastOutSlowInEasing)
            )
        }

        // Step 2: App Title smoothly slides up and fades in
        delay(120)
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
            )
        }
        launch {
            titleOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
            )
        }

        // Step 3: Tagline and bottom subtle indicators fade in
        delay(100)
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }
        launch {
            footerAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }

        // Finish splash with a fast, snappy total duration (~950ms)
        delay(550)
        onAnimationFinished?.invoke()
    }

    // Rich luxury background gradient:
    // Dark: Premium deep slate-teal (#0B1319 -> #0E171E -> #111C24, never pure harsh black)
    // Light: Crisp off-white & pale mint (#F8FAFB -> #F1F6F5 -> #EAF3F1)
    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0B1319),
                Color(0xFF0E171E),
                Color(0xFF111C24)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8FAFB),
                Color(0xFFF1F6F5),
                Color(0xFFEAF3F1)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        // Soft ambient aura blooming behind the emblem
        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(ambientPulseScale)
                .alpha(if (isDark) 0.20f else 0.14f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF14B8A6), // Vibrant Turquoise Mint
                            Color(0xFF0A8585),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Center Content Structure: Logo -> Name -> Tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            // App Logo Emblem
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .shadow(
                        elevation = if (isDark) 20.dp else 14.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Color(0xFF0A8585).copy(alpha = 0.45f),
                        ambientColor = Color(0xFF0A8585).copy(alpha = 0.25f)
                    )
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0D9488), // Rich Turquoise Teal
                                Color(0xFF0A8585), // Core Brand Accent
                                Color(0xFF065F58), // Deep Forest Teal
                                Color(0xFF044843)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(220f, 220f)
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.45f else 0.60f),
                                Color.White.copy(alpha = 0.08f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(100f, 200f)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                TaskMasterLogoGraphic(modifier = Modifier.size(56.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name with high-end typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = titleOffsetY.value.dp)
                    .alpha(titleAlpha.value)
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
                                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                            )
                        ) {
                            append("Manager")
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline: Organize • Focus • Complete
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.2.sp
                    ),
                    modifier = Modifier.alpha(taglineAlpha.value)
                )
            }
        }

        // Bottom Minimalist Modern Accent Bar
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
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Accent.copy(alpha = 0.35f))
                )
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Accent,
                                    Color(0xFF2DD4BF)
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Accent.copy(alpha = 0.35f))
                )
            }
        }
    }
}

/**
 * Modern, clean Task Manager vector emblem:
 * Features a stylized task sheet silhouette, precision tick mark,
 * task milestone bars, and a radiant glowing focus spark.
 */
@Composable
private fun TaskMasterLogoGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background subtle clip pad shape (task sheet card silhouette)
        val cardRect = RoundRect(
            left = w * 0.12f,
            top = h * 0.14f,
            right = w * 0.88f,
            bottom = h * 0.86f,
            cornerRadius = CornerRadius(w * 0.18f, h * 0.18f)
        )
        val cardPath = Path().apply { addRoundRect(cardRect) }
        drawPath(
            path = cardPath,
            color = Color.White.copy(alpha = 0.12f)
        )

        // Subtle upper task line
        val line1 = RoundRect(
            left = w * 0.26f,
            top = h * 0.28f,
            right = w * 0.58f,
            bottom = h * 0.34f,
            cornerRadius = CornerRadius(w * 0.03f, h * 0.03f)
        )
        drawPath(
            path = Path().apply { addRoundRect(line1) },
            color = Color.White.copy(alpha = 0.20f)
        )

        // Precision Main Checkmark
        val checkPath = Path().apply {
            moveTo(w * 0.26f, h * 0.52f)
            lineTo(w * 0.44f, h * 0.70f)
            lineTo(w * 0.76f, h * 0.34f)
        }

        // Checkmark subtle depth shadow
        drawPath(
            path = checkPath,
            color = Color(0xFF043834).copy(alpha = 0.5f),
            style = Stroke(
                width = w * 0.14f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Crisp White Checkmark
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(
                width = w * 0.12f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Radiant Glowing Mint Focus Spark on the checkmark tip
        drawCircle(
            color = Color(0xFF2DD4BF).copy(alpha = 0.6f), // Mint glow aura
            radius = w * 0.09f,
            center = Offset(w * 0.76f, h * 0.34f)
        )
        drawCircle(
            color = Color(0xFF5EEAD4), // Bright Mint
            radius = w * 0.06f,
            center = Offset(w * 0.76f, h * 0.34f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.03f,
            center = Offset(w * 0.76f, h * 0.34f)
        )
    }
}
