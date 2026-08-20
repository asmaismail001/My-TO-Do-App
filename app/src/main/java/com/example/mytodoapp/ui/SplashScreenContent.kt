package com.example.mytodoapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SplashContent() {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(500))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(700))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBEAE6)),
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
                    .size(140.dp)
                    .background(Color(0xFFE8A79B), RoundedCornerShape(34.dp))
            )
            Box(
                modifier = Modifier
                    .offset(x = 22.dp, y = (-92).dp)
                    .size(140.dp)
                    .background(Color(0xFF6B7F66), RoundedCornerShape(34.dp)),
                contentAlignment = Alignment.Center
            ) {
                CheckMark()
            }

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "My To-Do App",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                color = Color(0xFF3A342F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Small steps, big progress",
                color = Color(0xFF938A82),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CheckMark() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(60.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.55f)
            lineTo(size.width * 0.42f, size.height * 0.78f)
            lineTo(size.width * 0.85f, size.height * 0.28f)
        }
        drawPath(
            path = path,
            color = Color.White,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 14f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}