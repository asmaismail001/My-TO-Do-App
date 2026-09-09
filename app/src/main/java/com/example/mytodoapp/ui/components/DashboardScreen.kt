package com.example.mytodoapp.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.mytodoapp.R
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.*
import com.example.mytodoapp.util.CalendarUtil
import com.example.mytodoapp.viewmodel.DailyChartData
import com.example.mytodoapp.viewmodel.MonthlyChartData
import com.example.mytodoapp.viewmodel.DashboardPeriod
import com.example.mytodoapp.viewmodel.TodoViewModel
import com.example.mytodoapp.ui.weather.DashboardWeatherSection
import com.example.mytodoapp.ui.weather.WeatherDetailsDialog
import com.example.mytodoapp.ui.weather.LocationPickerDialog
import com.example.mytodoapp.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: TodoViewModel,
    profileViewModel: com.example.mytodoapp.viewmodel.ProfileViewModel,
    weatherViewModel: WeatherViewModel? = null,
    onToggle: (Todo) -> Unit,
    onEditClick: (Todo) -> Unit,
    onDeleteClick: (Todo) -> Unit,
    onAddTaskClick: () -> Unit,
    onFocusOpen: (Todo) -> Unit,
    onTodoClick: (Todo) -> Unit,
    onProfileClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current

    var showWeatherDetails by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val selectedDate = viewModel.dashboardDate
    val period = viewModel.dashboardPeriod

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val profileData = profileViewModel.profileData
    val profileBitmap = rememberBitmapFromUri(profileData?.profileImage)

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (hour) {
        in 0..11 -> stringResource(R.string.good_morning)
        in 12..16 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }

    // Calculations
    val stats = viewModel.getStatsForPeriod(period, selectedDate)
    val dailyTasks = viewModel.getTasksForDate(selectedDate)

    // Formatted Dates
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    val monthYearLabel = monthYearFormat.format(selectedDate.time)
    val fullDateLabel = fullDateFormat.format(selectedDate.time)

    // Week selector cards list (3 days before, selected day, 3 days after)
    val dateCards = remember(selectedDate) {
        (-3..3).map { offset ->
            (selectedDate.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, offset)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorFor(isDark)),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Header (Greeting & Today's Summary)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if ((profileData?.name?.length ?: 0) > 12) {
                                "$greeting,\n${profileData?.name ?: "User"}! 👋"
                            } else {
                                "$greeting, ${profileData?.name ?: "User"}! 👋"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 19.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryFor(isDark)
                            ),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = fullDateLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textMutedFor(isDark)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Profile Avatar navigating to Profile Screen on click
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Accent.copy(alpha = 0.15f))
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap,
                                contentDescription = stringResource(R.string.profile),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = profileData?.name?.firstOrNull()?.toString()?.uppercase() ?: "👤",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Accent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Summary Quick Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColorFor(isDark))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.todays_status),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = textPrimaryFor(isDark)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (dailyTasks.isEmpty()) {
                                    stringResource(R.string.no_tasks_today)
                                } else {
                                    val completed = dailyTasks.count { it.completed }
                                    stringResource(R.string.tasks_completed_summary, completed, dailyTasks.size)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryFor(isDark)
                            )
                        }
                    }
                }

                if (weatherViewModel != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val outdoorSummary = weatherViewModel.getOutdoorSummary(dailyTasks)
                    DashboardWeatherSection(
                        weatherUiState = weatherViewModel.currentWeatherState,
                        locationData = weatherViewModel.currentLocation,
                        outdoorSummary = outdoorSummary,
                        onClick = { showWeatherDetails = true },
                        onRefresh = { weatherViewModel.loadCurrentWeather(context, forceRefresh = true) }
                    )
                }
            }
        }

        // 2. Period Selector (Daily | Weekly | Monthly)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E7EB))
                    .padding(4.dp)
            ) {
                val periods = listOf(
                    DashboardPeriod.DAILY to stringResource(R.string.daily),
                    DashboardPeriod.WEEKLY to stringResource(R.string.weekly),
                    DashboardPeriod.MONTHLY to stringResource(R.string.monthly)
                )

                periods.forEach { (type, label) ->
                    val isSelected = period == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Accent else Color.Transparent
                            )
                            .clickable { viewModel.updateDashboardPeriod(type) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else textSecondaryFor(isDark),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // 3. Date Selection Row (Daily Mode weekday scroller / Monthly & Weekly arrows)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Header of date range selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // Let user open Native DatePickerDialog when clicking month-year label
                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                viewModel.updateDashboardDate(newCal)
                            },
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH)
                        )
                        datePickerDialog.show()
                    }
                ) {
                    Text(
                        text = when (period) {
                            DashboardPeriod.DAILY -> monthYearLabel
                            DashboardPeriod.WEEKLY -> {
                                val weekStart = viewModel.getStartOfWeek(selectedDate)
                                val weekEnd = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) }
                                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                                "${sdf.format(weekStart.time)} - ${sdf.format(weekEnd.time)}"
                            }
                            DashboardPeriod.MONTHLY -> monthYearLabel
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimaryFor(isDark)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.calendar),
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Simple Chevrons to scroll dates
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val newCal = (selectedDate.clone() as Calendar).apply {
                                when (period) {
                                    DashboardPeriod.DAILY -> add(Calendar.DAY_OF_MONTH, -1)
                                    DashboardPeriod.WEEKLY -> add(Calendar.DAY_OF_MONTH, -7)
                                    DashboardPeriod.MONTHLY -> add(Calendar.MONTH, -1)
                                }
                            }
                            viewModel.updateDashboardDate(newCal)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = stringResource(R.string.back),
                            tint = textSecondaryFor(isDark)
                        )
                    }

                    IconButton(
                        onClick = {
                            val newCal = (selectedDate.clone() as Calendar).apply {
                                when (period) {
                                    DashboardPeriod.DAILY -> add(Calendar.DAY_OF_MONTH, 1)
                                    DashboardPeriod.WEEKLY -> add(Calendar.DAY_OF_MONTH, 7)
                                    DashboardPeriod.MONTHLY -> add(Calendar.MONTH, 1)
                                }
                            }
                            viewModel.updateDashboardDate(newCal)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.done),
                            tint = textSecondaryFor(isDark)
                        )
                    }
                }
            }
        }

        // Horizontal weekday selector strip (Only visible in DAILY view)
        if (period == DashboardPeriod.DAILY) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dateCards) { date ->
                        val isSelected = CalendarUtil.isSameDay(date.timeInMillis, selectedDate.timeInMillis)
                        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
                        val dayOfWeek = dayOfWeekFormat.format(date.time)
                        val dayOfMonth = date.get(Calendar.DAY_OF_MONTH).toString()

                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) Accent else surfaceColorFor(isDark)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else cardBorderColorFor(isDark),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.updateDashboardDate(date) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayOfWeek,
                                    color = if (isSelected) Color.White else textMutedFor(isDark),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dayOfMonth,
                                    color = if (isSelected) Color.White else textPrimaryFor(isDark),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Statistics Block (Circular Completion Ring + Status Splits Card)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColorFor(isDark))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (period) {
                            DashboardPeriod.DAILY -> stringResource(R.string.daily_completion)
                            DashboardPeriod.WEEKLY -> stringResource(R.string.weekly_progress)
                            DashboardPeriod.MONTHLY -> stringResource(R.string.monthly_performance)
                            else -> stringResource(R.string.task_overview)
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimaryFor(isDark),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Circular progress ring
                        CompletionRing(
                            percentage = stats.completionPercentage,
                            modifier = Modifier.size(110.dp)
                        )

                        // Data Breakdown details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatItem(
                                label = stringResource(R.string.total_tasks),
                                count = stats.totalCount,
                                color = textPrimaryFor(isDark),
                                isDark = isDark
                            )
                            StatItem(
                                label = stringResource(R.string.completed),
                                count = stats.completedCount,
                                color = Accent,
                                isDark = isDark
                            )
                            StatItem(
                                label = stringResource(R.string.pending),
                                count = stats.pendingCount,
                                color = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                                isDark = isDark
                            )
                        }
                    }
                }
            }
        }

        // 5. Weekly Activity Chart (Only for WEEKLY) or Monthly Summary
        if (period == DashboardPeriod.WEEKLY) {
            item {
                val chartData = viewModel.getWeeklyChartData(selectedDate)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColorFor(isDark))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.weekly_activity),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = textPrimaryFor(isDark)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        WeeklyBarChart(data = chartData, isDark = isDark)
                    }
                }
            }
        } else if (period == DashboardPeriod.MONTHLY) {
            item {
                val monthlyChartData = viewModel.getMonthlyChartData(selectedDate)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColorFor(isDark))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.monthly_progress_graph),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = textPrimaryFor(isDark)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MonthlyBarChart(data = monthlyChartData, isDark = isDark)
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColorFor(isDark))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.monthly_insights),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = textPrimaryFor(isDark)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "In this month, you have completed ${stats.completedCount} tasks out of ${stats.totalCount} total (${stats.completionPercentage}%).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondaryFor(isDark),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // 6. Selected Date's Tasks Section Header & Items (Only shown in DAILY mode)
        if (period == DashboardPeriod.DAILY) {
            item {
                val formattedDate = SimpleDateFormat("MMM d", Locale.getDefault()).format(selectedDate.time)
                Text(
                    text = stringResource(R.string.tasks_for_date, formattedDate),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = textPrimaryFor(isDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 8.dp)
                )
            }

            // Selected Date's Task Items
            if (dailyTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "✦",
                                fontSize = 32.sp,
                                color = Accent
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.no_tasks_found),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = textPrimaryFor(isDark)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.no_tasks_today_msg),
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryFor(isDark),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(dailyTasks, key = { it.id }) { todo ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        TodoItemRow(
                            todo = todo,
                            onToggle = { onToggle(todo) },
                            onEditClick = { onEditClick(todo) },
                            onDeleteClick = { onDeleteClick(todo) },
                            onFocusClick = { onFocusOpen(todo) },
                            onTodoClick = { onTodoClick(todo) }
                        )
                    }
                }
            }
        }
    }

    if (showWeatherDetails && weatherViewModel != null) {
        WeatherDetailsDialog(
            weatherUiState = weatherViewModel.currentWeatherState,
            locationData = weatherViewModel.currentLocation,
            hourlyForecast = weatherViewModel.todayHourlyForecast,
            onChangeLocationClick = {
                showWeatherDetails = false
                showLocationPicker = true
            },
            onRefresh = { weatherViewModel.loadCurrentWeather(context, forceRefresh = true) },
            onDismiss = { showWeatherDetails = false }
        )
    }

    if (showLocationPicker && weatherViewModel != null) {
        LocationPickerDialog(
            searchResults = weatherViewModel.locationSearchResults,
            isSearching = weatherViewModel.isSearchingLocations,
            onSearchQueryChange = { weatherViewModel.searchCities(it) },
            onLocationSelected = { loc ->
                weatherViewModel.setManualLocation(loc.latitude, loc.longitude, loc.locationName)
            },
            onUseGpsClick = {
                weatherViewModel.useDeviceLocation(context)
            },
            onDismiss = {
                showLocationPicker = false
                weatherViewModel.clearLocationSearchResults()
            }
        )
    }
}

@Composable
fun CompletionRing(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    val animatePercentage = animateFloatAsState(
        targetValue = percentage.toFloat() / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "percentage"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val isDark = LocalIsDarkTheme.current
        val ringTrack = if (isDark) Color(0xFF242424) else Color(0xFFEEF0F3)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            // Track Ring
            drawCircle(
                color = ringTrack,
                style = Stroke(width = strokeWidth)
            )

            // Progress Arc
            drawArc(
                color = Accent,
                startAngle = -90f,
                sweepAngle = animatePercentage.value * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Percentage Text in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$percentage%",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textPrimaryFor(isDark)
            )
            Text(
                text = "Completed",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = textSecondaryFor(isDark)
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    count: Int,
    color: Color,
    isDark: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textMutedFor(isDark)
            )
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = textPrimaryFor(isDark)
            )
        }
    }
}

@Composable
fun WeeklyBarChart(
    data: List<DailyChartData>,
    isDark: Boolean
) {
    val maxTasks = data.maxOf { it.completedCount + it.pendingCount }.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        for (dayData in data) {
            val total = dayData.completedCount + dayData.pendingCount
            val fraction = total.toFloat() / maxTasks.toFloat()
            val completedFraction = if (total > 0) dayData.completedCount.toFloat() / total.toFloat() else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Stacked Bar Draw
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (total > 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight(fraction)
                                .width(12.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (isDark) Color(0xFF242424) else Color(0xFFE5E7EB))
                        ) {
                            // Pending Portion
                            Box(
                                modifier = Modifier
                                    .weight((1f - completedFraction).coerceAtLeast(0.01f))
                                    .fillMaxWidth()
                            )
                            // Completed Portion
                            Box(
                                modifier = Modifier
                                    .weight(completedFraction.coerceAtLeast(0.01f))
                                    .fillMaxWidth()
                                    .background(Accent)
                            )
                        }
                    } else {
                        // Empty outline indicator
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFEEF0F3))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dayData.dayAbbreviation,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = textMutedFor(isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun rememberBitmapFromUri(uriStr: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(uriStr) {
        if (uriStr == null) return@remember null
        try {
            val uri = uriStr.toUri()
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            bitmap.asImageBitmap()
        } catch (_: java.lang.Exception) {
            null
        }
    }
}

@Composable
fun MonthlyBarChart(
    data: List<MonthlyChartData>,
    isDark: Boolean
) {
    val maxTasks = data.maxOf { it.completedCount + it.pendingCount }.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        for (segmentData in data) {
            val total = segmentData.completedCount + segmentData.pendingCount
            val fraction = total.toFloat() / maxTasks.toFloat()
            val completedFraction = if (total > 0) segmentData.completedCount.toFloat() / total.toFloat() else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Stacked Bar Draw
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (total > 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight(fraction)
                                .width(16.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (isDark) Color(0xFF222836) else Color(0xFFE5E7EB))
                        ) {
                            // Pending Portion
                            Box(
                                modifier = Modifier
                                    .weight((1f - completedFraction).coerceAtLeast(0.01f))
                                    .fillMaxWidth()
                            )
                            // Completed Portion
                            Box(
                                modifier = Modifier
                                    .weight(completedFraction.coerceAtLeast(0.01f))
                                    .fillMaxWidth()
                                    .background(Accent)
                            )
                        }
                    } else {
                        // Empty outline indicator
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isDark) Color(0xFF1A212D) else Color(0xFFEEF0F3))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = segmentData.segmentLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = textMutedFor(isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}