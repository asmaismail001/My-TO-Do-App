package com.example.mytodoapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) { scale.animateTo(1f, animationSpec = tween(600)) }
    LaunchedEffect(Unit) { alpha.animateTo(1f, animationSpec = tween(800)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
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
                    .size(96.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2BB3A0), Color(0xFF0F6B5C))
                        ),
                        RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                SplashCheckLogo(modifier = Modifier.size(52.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                    ) {
                        append("Task")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    ) {
                        append("Manager")
                    }
                },
                fontSize = 28.sp,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "PLAN. TRACK. ACHIEVE.",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
private fun SplashCheckLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val check = Path().apply {
            moveTo(w * 0.22f, h * 0.52f)
            cubicTo(
                w * 0.32f, h * 0.68f,
                w * 0.40f, h * 0.78f,
                w * 0.46f, h * 0.74f
            )
            cubicTo(
                w * 0.56f, h * 0.58f,
                w * 0.68f, h * 0.38f,
                w * 0.80f, h * 0.24f
            )
        }
        drawPath(
            path = check,
            color = Color.White,
            style = Stroke(width = w * 0.13f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = w * 0.09f,
            center = Offset(w * 0.80f, h * 0.22f)
        )
    }
}
