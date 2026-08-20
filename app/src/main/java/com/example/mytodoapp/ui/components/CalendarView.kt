package com.example.mytodoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.SageGreen
import com.example.mytodoapp.ui.SalmonPink
import com.example.mytodoapp.ui.TextMuted
import com.example.mytodoapp.ui.TextPrimary
import com.example.mytodoapp.ui.TextSecondary
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
    Column(modifier = Modifier.fillMaxWidth()) {

        // Month header with navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                onMonthChange(newMonth)
            }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = TextSecondary)
            }
            Text(
                text = CalendarUtil.monthYearLabel(visibleMonth),
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = {
                val newMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                onMonthChange(newMonth)
            }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month", tint = TextSecondary)
            }
        }

        // Weekday labels
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(day, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val gridDays = CalendarUtil.getMonthGrid(visibleMonth)

        // 6 rows of 7 days
        for (week in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
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
                            .padding(2.dp)
                            .background(
                                when {
                                    isSelected -> SageGreen
                                    isToday -> SageGreen.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .clickable { onDaySelected(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.get(Calendar.DAY_OF_MONTH).toString(),
                                color = when {
                                    isSelected -> Color.White
                                    !inCurrentMonth -> TextMuted.copy(alpha = 0.4f)
                                    else -> TextPrimary
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (hasTasks) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(4.dp)
                                        .background(
                                            if (isSelected) Color.White else SalmonPink,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

