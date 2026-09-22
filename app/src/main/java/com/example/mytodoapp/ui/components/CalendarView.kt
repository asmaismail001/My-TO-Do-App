package com.example.mytodoapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColorFor(isDark))
            .border(1.dp, cardBorderColorFor(isDark), RoundedCornerShape(20.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp)
    ) {
        // Month Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    onMonthChange(newMonth)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.back),
                    tint = textSecondaryFor(isDark)
                )
            }

            AnimatedContent(
                targetState = CalendarUtil.monthYearLabel(visibleMonth),
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "monthLabelAnim"
            ) { label ->
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryFor(isDark),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp)
                )
            }

            IconButton(
                onClick = {
                    val newMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    onMonthChange(newMonth)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = stringResource(R.string.done),
                    tint = textSecondaryFor(isDark)
                )
            }
        }

        // Weekday Labels Row
        val weekdaySymbols = remember {
            val dfs = java.text.DateFormatSymbols.getInstance(java.util.Locale.getDefault())
            val shortWeekdays = dfs.shortWeekdays
            listOf(
                shortWeekdays.getOrElse(Calendar.SUNDAY) { "S" }.take(2),
                shortWeekdays.getOrElse(Calendar.MONDAY) { "M" }.take(2),
                shortWeekdays.getOrElse(Calendar.TUESDAY) { "T" }.take(2),
                shortWeekdays.getOrElse(Calendar.WEDNESDAY) { "W" }.take(2),
                shortWeekdays.getOrElse(Calendar.THURSDAY) { "T" }.take(2),
                shortWeekdays.getOrElse(Calendar.FRIDAY) { "F" }.take(2),
                shortWeekdays.getOrElse(Calendar.SATURDAY) { "S" }.take(2)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            for (day in weekdaySymbols) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        color = textMutedFor(isDark),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp)
                    )
                }
            }
        }

        // Animated Day Grid
        AnimatedContent(
            targetState = visibleMonth.get(Calendar.MONTH) to visibleMonth.get(Calendar.YEAR),
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "monthGridAnim"
        ) { _ ->
            val gridDays = CalendarUtil.getMonthGrid(visibleMonth)

            Column(modifier = Modifier.fillMaxWidth()) {
                for (week in 0 until 6) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayIndex in 0 until 7) {
                            val day = gridDays[week * 7 + dayIndex]
                            val inCurrentMonth = CalendarUtil.isSameMonth(day, visibleMonth)
                            val isSelected = CalendarUtil.isSameDay(day.timeInMillis, selectedDay.timeInMillis)
                            val isToday = CalendarUtil.isSameDay(day.timeInMillis, Calendar.getInstance().timeInMillis)

                            // Accurate date matching: only dates with tasks on this exact day have hasTasks = true
                            val hasTasks = tasks.any { todo ->
                                val taskTime = todo.dueTimeMillis ?: todo.createdAt
                                CalendarUtil.isSameDay(taskTime, day.timeInMillis)
                            }

                            val dayBgColor by animateColorAsState(
                                targetValue = when {
                                    isSelected -> Accent
                                    isToday -> Accent.copy(alpha = 0.12f)
                                    else -> Color.Transparent
                                },
                                animationSpec = tween(durationMillis = 200),
                                label = "dayBgColor"
                            )
                            val dayTextColor by animateColorAsState(
                                targetValue = when {
                                    isSelected -> Color.White
                                    isToday -> Accent
                                    !inCurrentMonth -> textMutedFor(isDark).copy(alpha = 0.35f)
                                    else -> textPrimaryFor(isDark)
                                },
                                animationSpec = tween(durationMillis = 200),
                                label = "dayTextColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(dayBgColor, shape = CircleShape)
                                        .clickable { onDaySelected(day) },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = day.get(Calendar.DAY_OF_MONTH).toString(),
                                        color = dayTextColor,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                    )

                                    // Subtle task indicator dot only when tasks exist on this date
                                    if (hasTasks) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val dotColor by animateColorAsState(
                                            targetValue = if (isSelected) Color.White else Accent,
                                            animationSpec = tween(200),
                                            label = "dotColor"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}