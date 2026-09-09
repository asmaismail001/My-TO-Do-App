package com.example.mytodoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mytodoapp.R
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.LocalIsDarkTheme
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.dialogFieldBackgroundFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CustomDateTimePickerDialog(
    initialTime: Long? = null,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    // Initialize Calendar from initialTime
    val calendar = remember {
        Calendar.getInstance().apply {
            initialTime?.let {
                if (it > 0) timeInMillis = it
            }
        }
    }

    // Reactive Compose states for date selection
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    var hourInput by remember {
        val hr = calendar.get(Calendar.HOUR)
        val hrStr = if (hr == 0) "12" else hr.toString().padStart(2, '0')
        mutableStateOf(hrStr)
    }

    var minuteInput by remember {
        val min = calendar.get(Calendar.MINUTE)
        mutableStateOf(min.toString().padStart(2, '0'))
    }

    var isAm by remember {
        mutableStateOf(calendar.get(Calendar.AM_PM) == Calendar.AM)
    }

    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = surfaceColorFor(isDark),
            border = BorderStroke(1.dp, Accent.copy(alpha = 0.3f)),
            tonalElevation = 8.dp,
            modifier = Modifier.width(328.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Title
                Text(
                    text = stringResource(R.string.select_date_time),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = Accent
                )

                // Date Section Header
                Text(
                    text = "DATE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = textMutedFor(isDark),
                    fontWeight = FontWeight.Bold
                )

                // Date Selector Box (displays the reactive date state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = cardBorderColorFor(isDark),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dateSdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
                    val displayCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, selectedDay)
                    }
                    Text(
                        text = dateSdf.format(displayCal.time),
                        color = textPrimaryFor(isDark),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = stringResource(R.string.select_date_time),
                        tint = Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Time Section Header
                Text(
                    text = "TIME",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = textMutedFor(isDark),
                    fontWeight = FontWeight.Bold
                )

                // Time Input Row (Hour : Minute + AM/PM)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Hour Box
                    TimeInputBox(
                        value = hourInput,
                        onValueChange = { input ->
                            val sanitized = input.filter { it.isDigit() }
                            if (sanitized.length <= 2) {
                                val num = sanitized.toIntOrNull()
                                if (num == null || num in 0..12) {
                                    hourInput = sanitized
                                }
                            }
                        },
                        label = "Hour",
                        isDark = isDark
                    )

                    // Separator
                    Text(
                        text = ":",
                        color = textPrimaryFor(isDark),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp) // Align with inputs
                    )

                    // Minute Box
                    TimeInputBox(
                        value = minuteInput,
                        onValueChange = { input ->
                            val sanitized = input.filter { it.isDigit() }
                            if (sanitized.length <= 2) {
                                val num = sanitized.toIntOrNull()
                                if (num == null || num in 0..59) {
                                    minuteInput = sanitized
                                }
                            }
                        },
                        label = "Minute",
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // AM/PM Selector Segmented Control
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 16.dp), // Align with inputs
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(
                                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = cardBorderColorFor(isDark),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // AM Segment
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (isAm) Accent else Color.Transparent,
                                        shape = RoundedCornerShape(9.dp)
                                    )
                                    .clickable { isAm = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AM",
                                    color = if (isAm) Color.White else textSecondaryFor(isDark),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // PM Segment
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (!isAm) Accent else Color.Transparent,
                                        shape = RoundedCornerShape(9.dp)
                                    )
                                    .clickable { isAm = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PM",
                                    color = if (!isAm) Color.White else textSecondaryFor(isDark),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = textSecondaryFor(isDark),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val hr = hourInput.toIntOrNull() ?: 12
                            val min = minuteInput.toIntOrNull() ?: 0
                            val finalHr = if (hr !in 1..12) 12 else hr
                            val finalMin = if (min !in 0..59) 0 else min

                            // Calculate 24-hour hour value
                            val hour24 = when {
                                isAm && finalHr == 12 -> 0
                                isAm -> finalHr
                                !isAm && finalHr == 12 -> 12
                                else -> finalHr + 12
                            }

                            val resultCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYear)
                                set(Calendar.MONTH, selectedMonth)
                                set(Calendar.DAY_OF_MONTH, selectedDay)
                                set(Calendar.HOUR_OF_DAY, hour24)
                                set(Calendar.MINUTE, finalMin)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onSave(resultCal.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Custom Compact Compose Date Picker
    if (showDatePicker) {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        CompactDatePickerDialog(
            initialDateMillis = tempCal.timeInMillis,
            onDismiss = { showDatePicker = false },
            onDateSelected = { pickedMillis ->
                val resCal = Calendar.getInstance().apply {
                    timeInMillis = pickedMillis
                }
                selectedYear = resCal.get(Calendar.YEAR)
                selectedMonth = resCal.get(Calendar.MONTH)
                selectedDay = resCal.get(Calendar.DAY_OF_MONTH)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun CompactDatePickerDialog(
    initialDateMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    // Calendar for tracking current month/year view navigation
    val viewingCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialDateMillis
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    var currentMonth by remember { mutableStateOf(viewingCal.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(viewingCal.get(Calendar.YEAR)) }

    // Calendar representing selected date
    val selectedCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialDateMillis
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    var selectedDayState by remember { mutableStateOf(selectedCal.get(Calendar.DAY_OF_MONTH)) }
    var selectedMonthState by remember { mutableStateOf(selectedCal.get(Calendar.MONTH)) }
    var selectedYearState by remember { mutableStateOf(selectedCal.get(Calendar.YEAR)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = surfaceColorFor(isDark),
            border = BorderStroke(1.dp, Accent.copy(alpha = 0.3f)),
            tonalElevation = 8.dp,
            modifier = Modifier.width(300.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Title
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Accent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Month navigation row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewingCal.add(Calendar.MONTH, -1)
                            currentMonth = viewingCal.get(Calendar.MONTH)
                            currentYear = viewingCal.get(Calendar.YEAR)
                        }
                    ) {
                        Text("‹", fontSize = 28.sp, color = textSecondaryFor(isDark), fontWeight = FontWeight.Bold)
                    }

                    val monthNameSdf = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
                    val viewingMonthCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    Text(
                        text = monthNameSdf.format(viewingMonthCal.time),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryFor(isDark)
                    )

                    IconButton(
                        onClick = {
                            viewingCal.add(Calendar.MONTH, 1)
                            currentMonth = viewingCal.get(Calendar.MONTH)
                            currentYear = viewingCal.get(Calendar.YEAR)
                        }
                    ) {
                        Text("›", fontSize = 28.sp, color = textSecondaryFor(isDark), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weekdays abbreviation row (S M T W T F S)
                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { dayName ->
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textMutedFor(isDark),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid calculations for month cells
                val cells = remember(currentMonth, currentYear) {
                    val list = mutableListOf<Calendar?>()
                    val firstDayCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 12)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
                    val leadingNulls = startDayOfWeek - 1

                    for (i in 0 until leadingNulls) {
                        list.add(null)
                    }

                    val maxDays = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    for (day in 1..maxDays) {
                        val dayCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentYear)
                            set(Calendar.MONTH, currentMonth)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, 12)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        list.add(dayCal)
                    }

                    while (list.size % 7 != 0) {
                        list.add(null)
                    }
                    list
                }

                // Render grid
                val rowCount = cells.size / 7
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in 0 until rowCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (c in 0 until 7) {
                                val cellCal = cells[r * 7 + c]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cellCal != null) {
                                        val day = cellCal.get(Calendar.DAY_OF_MONTH)
                                        val isSelected = selectedDayState == day &&
                                                selectedMonthState == currentMonth &&
                                                selectedYearState == currentYear

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    color = if (isSelected) Accent else Color.Transparent,
                                                    shape = RoundedCornerShape(50)
                                                )
                                                .clickable {
                                                    selectedDayState = day
                                                    selectedMonthState = currentMonth
                                                    selectedYearState = currentYear
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                color = if (isSelected) Color.White else textPrimaryFor(isDark),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = textSecondaryFor(isDark),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val result = Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYearState)
                                set(Calendar.MONTH, selectedMonthState)
                                set(Calendar.DAY_OF_MONTH, selectedDayState)
                                set(Calendar.HOUR_OF_DAY, 12)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onDateSelected(result.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Select",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isDark: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 46.dp)
                .background(
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Accent else cardBorderColorFor(isDark),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = LocalTextStyle.current.copy(
                    color = textPrimaryFor(isDark),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(Accent),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "00",
                                color = textMutedFor(isDark),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textMutedFor(isDark)
        )
    }
}
