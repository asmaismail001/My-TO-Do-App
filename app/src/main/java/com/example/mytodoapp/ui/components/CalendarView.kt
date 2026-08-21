package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor
import com.example.mytodoapp.util.CalendarUtil
import java.util.Calendar

@Composable
fun CalendarView(
    tasks: List<Todo>,
    selectedDay: Calendar,
    onDaySelected: (Calendar) -> Unit,
    visibleMonth: Calendar,
    onMonthChange: (Calendar) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColorFor(isDark))
            .border(1.dp, cardBorderColorFor(isDark), RoundedCornerShape(20.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        // Month Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                onMonthChange(newMonth)
            }) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Previous month",
                    tint = textSecondaryFor(isDark)
                )
            }
            Text(
                text = CalendarUtil.monthYearLabel(visibleMonth),
                fontWeight = FontWeight.Bold,
                color = textPrimaryFor(isDark),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = {
                val newMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                onMonthChange(newMonth)
            }) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Next month",
                    tint = textSecondaryFor(isDark)
                )
            }
        }

        // Weekday Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        color = textMutedFor(isDark),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                }
            }
        }

        val gridDays = CalendarUtil.getMonthGrid(visibleMonth)

        // Day Grid
        for (week in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayIndex in 0 until 7) {
                    val day = gridDays[week * 7 + dayIndex]
                    val inCurrentMonth = CalendarUtil.isSameMonth(day, visibleMonth)
                    val isSelected = CalendarUtil.isSameDay(day.timeInMillis, selectedDay.timeInMillis)
                    val isToday = CalendarUtil.isSameDay(day.timeInMillis, Calendar.getInstance().timeInMillis)

                    val hasTasks = tasks.any {
                        it.dueTimeMillis != null && CalendarUtil.isSameDay(it.dueTimeMillis, day.timeInMillis)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .clickable { onDaySelected(day) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        when {
                                            isSelected -> Accent
                                            isToday -> Accent.copy(alpha = 0.12f)
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.get(Calendar.DAY_OF_MONTH).toString(),
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> Accent
                                        !inCurrentMonth -> textMutedFor(isDark).copy(alpha = 0.3f)
                                        else -> textPrimaryFor(isDark)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            if (hasTasks) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            if (isSelected) Accent else Accent.copy(alpha = 0.6f),
                                            shape = CircleShape
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}