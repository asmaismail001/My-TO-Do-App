package com.example.mytodoapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.ui.TextGrey

@Composable
fun TodoProgressText(completedCount: Int, total: Int) {
    Text(
        text = "$completedCount of $total completed",
        color = TextGrey,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}