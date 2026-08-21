package com.example.mytodoapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashContent() {
    val isDark = isSystemInDarkTheme()
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) { scale.animateTo(1f, animationSpec = tween(600)) }
    LaunchedEffect(Unit) { alpha.animateTo(1f, animationSpec = tween(800)) }

    val bgBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0E1516), Color(0xFF152022)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF4F6F6)))
    }

    val logoBrush = Brush.linearGradient(
        listOf(Accent, Color(0xFF0F766E))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(logoBrush, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                ProductivityLogo(modifier = Modifier.size(56.dp))
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.W300, color = if (isDark) Color(0xFF99F6E4) else Accent)) {
                        append("Task")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.W800, color = if (isDark) Color.White else Color(0xFF111827))) {
                        append("Manager")
                    }
                },
                fontSize = 28.sp,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Plan. Track. Achieve.",
                color = if (isDark) Color(0xFF6B7280) else Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun ProductivityLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Draw main progress curve (thick white path)
        val path1 = Path().apply {
            moveTo(w * 0.25f, h * 0.58f)
            cubicTo(
                w * 0.35f, h * 0.72f,
                w * 0.48f, h * 0.75f,
                w * 0.75f, h * 0.32f
            )
        }
        drawPath(
            path = path1,
            color = Color.White,
            style = Stroke(width = w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Draw second curve representing focus/double-check (mint/light teal path)
        val path2 = Path().apply {
            moveTo(w * 0.42f, h * 0.60f)
            cubicTo(
                w * 0.48f, h * 0.68f,
                w * 0.55f, h * 0.68f,
                w * 0.65f, h * 0.48f
            )
        }
        drawPath(
            path = path2,
            color = Color(0xFFCCFBF1).copy(alpha = 0.8f),
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Draw target dot representing goals/achievements (gold/amber)
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = w * 0.07f,
            center = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.22f)
        )
    }
}