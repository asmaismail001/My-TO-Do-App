package com.example.mytodoapp.ui.components

import android.app.DatePickerDialog
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.mytodoapp.R
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.model.WeatherUiState
import com.example.mytodoapp.ui.*
import com.example.mytodoapp.ui.weather.LocationPickerDialog
import com.example.mytodoapp.ui.weather.WeatherDetailsDialog
import com.example.mytodoapp.ui.weather.WeatherIcon
import com.example.mytodoapp.util.CalendarUtil
import com.example.mytodoapp.viewmodel.DailyChartData
import com.example.mytodoapp.viewmodel.DashboardPeriod
import com.example.mytodoapp.viewmodel.MonthlyChartData
import com.example.mytodoapp.viewmodel.ProfileViewModel
import com.example.mytodoapp.viewmodel.TodoViewModel
import com.example.mytodoapp.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: TodoViewModel,
    profileViewModel: ProfileViewModel,
    weatherViewModel: WeatherViewModel? = null,
    onToggle: (Todo) -> Unit,
    onEditClick: (Todo) -> Unit,
    onDeleteClick: (Todo) -> Unit,
    onAddTaskClick: () -> Unit = {},
    onFocusOpen: (Todo) -> Unit,
    onTodoClick: (Todo) -> Unit,
    onProfileClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Smooth staggered entrance animations
    val greetingAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "greetingAlpha"
    )
    val greetingOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 18f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "greetingOffsetY"
    )

    val searchAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 380, delayMillis = 80, easing = FastOutSlowInEasing),
        label = "searchAlpha"
    )
    val searchOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 18f,
        animationSpec = tween(durationMillis = 380, delayMillis = 80, easing = FastOutSlowInEasing),
        label = "searchOffsetY"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "cardAlpha"
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 20f,
        animationSpec = tween(durationMillis = 400, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "cardOffsetY"
    )

    var showWeatherDetails by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var isDateDropdownExpanded by remember { mutableStateOf(false) }

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

    val currentPeriodTasks = when (period) {
        DashboardPeriod.DAILY -> dailyTasks
        DashboardPeriod.WEEKLY -> viewModel.getTasksForPeriod(DashboardPeriod.WEEKLY, selectedDate)
        DashboardPeriod.MONTHLY -> viewModel.getTasksForPeriod(DashboardPeriod.MONTHLY, selectedDate)
    }
    val displayedTasks = if (viewModel.isFilterActive) {
        val matched = viewModel.filterTasks(currentPeriodTasks)
        if (matched.isEmpty() && currentPeriodTasks.isEmpty()) viewModel.filterTasks(viewModel.todoList) else matched
    } else {
        currentPeriodTasks
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorFor(isDark)),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. GOOD MORNING CARD (Smooth Entrance Animation)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        alpha = greetingAlpha
                        translationY = greetingOffsetY * density
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                border = BorderStroke(1.dp, cardBorderColorFor(isDark))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = textSecondaryFor(isDark),
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "${profileData?.name ?: "User"}! 👋",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryFor(isDark)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // Subtitle: Date & Compact Weather Action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = fullDateLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textMutedFor(isDark)
                                )
                            )

                            if (weatherViewModel != null) {
                                val weatherState = weatherViewModel.currentWeatherState
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0xFF182228) else Color(0xFFEBF5F5),
                                    border = BorderStroke(0.8.dp, Accent.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showWeatherDetails = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        when (weatherState) {
                                            is WeatherUiState.Loading -> {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(11.dp),
                                                    color = Accent,
                                                    strokeWidth = 1.3.dp
                                                )
                                            }
                                            is WeatherUiState.Success -> {
                                                val data = weatherState.data
                                                WeatherIcon(iconType = data.condition.iconType, modifier = Modifier.size(13.dp))
                                                Text(
                                                    text = "${data.temperatureC.toInt()}°C",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                                    color = textPrimaryFor(isDark)
                                                )
                                            }
                                            else -> {
                                                Text(text = "🌤️", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Accent.copy(alpha = 0.15f))
                            .border(1.5.dp, Accent.copy(alpha = 0.45f), CircleShape)
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
            }
        }

        // 2. SEARCH BAR (Smooth Entrance Animation)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = searchAlpha
                        translationY = searchOffsetY * density
                    }
            ) {
                TodoSearchBar(
                    query = viewModel.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    selectedPriority = viewModel.selectedPriorityFilter,
                    onPrioritySelect = { viewModel.onPriorityFilterChange(it) },
                    selectedTag = viewModel.selectedTagFilter,
                    onTagSelect = { viewModel.onTagFilterChange(it) },
                    availableTags = viewModel.allUniqueTags,
                    onClearFilters = { viewModel.clearFilters() },
                    showFiltersRow = true
                )
            }
        }

        // 3. ONE MAIN DASHBOARD CARD (Smooth Entrance & Interactive Transitions)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        alpha = cardAlpha
                        translationY = cardOffsetY * density
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                border = BorderStroke(1.dp, cardBorderColorFor(isDark))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // A. Daily / Weekly / Monthly Horizontal Segmented Filter (Smooth Animated Transition)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val periods = listOf(
                            DashboardPeriod.DAILY to stringResource(R.string.daily),
                            DashboardPeriod.WEEKLY to stringResource(R.string.weekly),
                            DashboardPeriod.MONTHLY to stringResource(R.string.monthly)
                        )

                        periods.forEach { (type, label) ->
                            val isSelected = period == type
                            val buttonBgColor by animateColorAsState(
                                targetValue = if (isSelected) Accent else (if (isDark) Color(0xFF242424) else Color(0xFFF1F3F5)),
                                animationSpec = tween(durationMillis = 220),
                                label = "periodBg"
                            )
                            val buttonTextColor by animateColorAsState(
                                targetValue = if (isSelected) Color.White else textSecondaryFor(isDark),
                                animationSpec = tween(durationMillis = 220),
                                label = "periodText"
                            )
                            val buttonBorderColor by animateColorAsState(
                                targetValue = if (isSelected) Accent else cardBorderColorFor(isDark),
                                animationSpec = tween(durationMillis = 220),
                                label = "periodBorder"
                            )

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.updateDashboardPeriod(type) },
                                shape = RoundedCornerShape(10.dp),
                                color = buttonBgColor,
                                border = BorderStroke(width = 1.dp, color = buttonBorderColor)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = label,
                                        color = buttonTextColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                                    )
                                }
                            }
                        }
                    }

                    // B. Date Selector Control
                    if (period == DashboardPeriod.DAILY) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val arrowRotation by animateFloatAsState(
                                targetValue = if (isDateDropdownExpanded) 180f else 0f,
                                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                label = "arrowRotation"
                            )

                            // Dropdown Trigger (Closed by default!)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xFF242424) else Color(0xFFF1F4F5))
                                    .clickable { isDateDropdownExpanded = !isDateDropdownExpanded }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    val displayDateText = if (CalendarUtil.isToday(selectedDate.timeInMillis)) {
                                        "Today (${SimpleDateFormat("MMM d", Locale.getDefault()).format(selectedDate.time)})"
                                    } else {
                                        fullDateLabel
                                    }
                                    Text(
                                        text = displayDateText,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                        color = textPrimaryFor(isDark)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isDateDropdownExpanded) stringResource(R.string.hide_dates) else stringResource(R.string.select_date),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Accent
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Accent,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .rotate(arrowRotation)
                                    )
                                }
                            }

                            // Collapsible Date Boxes (Smooth Animated Expand/Collapse)
                            AnimatedVisibility(
                                visible = isDateDropdownExpanded,
                                enter = expandVertically(animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(220)),
                                exit = shrinkVertically(animationSpec = tween(240, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(180))
                            ) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(dateCards) { date ->
                                        val isSelected = CalendarUtil.isSameDay(date.timeInMillis, selectedDate.timeInMillis)
                                        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
                                        val dayOfWeek = dayOfWeekFormat.format(date.time)
                                        val dayOfMonth = date.get(Calendar.DAY_OF_MONTH).toString()

                                        // Accurate task existence check for subtle indicator dot
                                        val dateHasTasks = viewModel.todoList.any { todo ->
                                            val taskTime = todo.dueTimeMillis ?: todo.createdAt
                                            CalendarUtil.isSameDay(taskTime, date.timeInMillis)
                                        }

                                        val dateBgColor by animateColorAsState(
                                            targetValue = if (isSelected) Accent else (if (isDark) Color(0xFF242424) else Color(0xFFF1F3F5)),
                                            animationSpec = tween(durationMillis = 200),
                                            label = "dateBg"
                                        )
                                        val dateTextColor by animateColorAsState(
                                            targetValue = if (isSelected) Color.White else textPrimaryFor(isDark),
                                            animationSpec = tween(durationMillis = 200),
                                            label = "dateText"
                                        )
                                        val dayLabelColor by animateColorAsState(
                                            targetValue = if (isSelected) Color.White.copy(alpha = 0.9f) else textMutedFor(isDark),
                                            animationSpec = tween(durationMillis = 200),
                                            label = "dayLabel"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .width(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(dateBgColor)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) Color.Transparent else cardBorderColorFor(isDark),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    viewModel.updateDashboardDate(date)
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayOfWeek,
                                                    color = dayLabelColor,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = dayOfMonth,
                                                    color = dateTextColor,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (dateHasTasks) {
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
                    } else {
                        // Weekly / Monthly Header Navigation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF242424) else Color(0xFFF1F4F5))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
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
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = stringResource(R.string.calendar),
                                    tint = Accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (period) {
                                        DashboardPeriod.WEEKLY -> {
                                            val weekStart = viewModel.getStartOfWeek(selectedDate)
                                            val weekEnd = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) }
                                            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                                            "${sdf.format(weekStart.time)} - ${sdf.format(weekEnd.time)}"
                                        }
                                        DashboardPeriod.MONTHLY -> monthYearLabel
                                        else -> monthYearLabel
                                    },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                    color = textPrimaryFor(isDark)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val newCal = (selectedDate.clone() as Calendar).apply {
                                            when (period) {
                                                DashboardPeriod.WEEKLY -> add(Calendar.DAY_OF_MONTH, -7)
                                                DashboardPeriod.MONTHLY -> add(Calendar.MONTH, -1)
                                                else -> {}
                                            }
                                        }
                                        viewModel.updateDashboardDate(newCal)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = stringResource(R.string.back),
                                        tint = textSecondaryFor(isDark),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val newCal = (selectedDate.clone() as Calendar).apply {
                                            when (period) {
                                                DashboardPeriod.WEEKLY -> add(Calendar.DAY_OF_MONTH, 7)
                                                DashboardPeriod.MONTHLY -> add(Calendar.MONTH, 1)
                                                else -> {}
                                            }
                                        }
                                        viewModel.updateDashboardDate(newCal)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.done),
                                        tint = textSecondaryFor(isDark),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = cardBorderColorFor(isDark).copy(alpha = 0.5f),
                        thickness = 1.dp
                    )

                    // C. Completion & Progress Statistics
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (period) {
                                    DashboardPeriod.DAILY -> stringResource(R.string.daily_completion)
                                    DashboardPeriod.WEEKLY -> stringResource(R.string.weekly_progress)
                                    DashboardPeriod.MONTHLY -> stringResource(R.string.monthly_performance)
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
                                color = textPrimaryFor(isDark)
                            )

                            Text(
                                text = "${stats.completedCount} / ${stats.totalCount} completed",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Accent
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0xFF242424) else Color(0xFFF8FAFB))
                                .border(1.dp, cardBorderColorFor(isDark).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CompletionRing(
                                percentage = stats.completionPercentage,
                                modifier = Modifier.size(80.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                    // Weekly Chart (in Weekly view)
                    if (period == DashboardPeriod.WEEKLY) {
                        val chartData = viewModel.getWeeklyChartData(selectedDate)
                        Text(
                            text = stringResource(R.string.weekly_activity),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                            color = textPrimaryFor(isDark)
                        )
                        WeeklyBarChart(data = chartData, isDark = isDark)
                    } else if (period == DashboardPeriod.MONTHLY) {
                        // Monthly Chart & Insights
                        val monthlyChartData = viewModel.getMonthlyChartData(selectedDate)
                        Text(
                            text = stringResource(R.string.monthly_progress_graph),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                            color = textPrimaryFor(isDark)
                        )
                        MonthlyBarChart(data = monthlyChartData, isDark = isDark)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Accent.copy(alpha = if (isDark) 0.12f else 0.08f))
                                .border(1.dp, Accent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.monthly_insights),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                    color = Accent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "In this month, you have completed ${stats.completedCount} tasks out of ${stats.totalCount} total (${stats.completionPercentage}%).",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = textSecondaryFor(isDark),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // D. Tasks Section (Divider & Content)
                    if (period == DashboardPeriod.DAILY || viewModel.isFilterActive) {
                        HorizontalDivider(
                            color = cardBorderColorFor(isDark).copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        // Tasks Section Header (Without Add Task text button)
                        val formattedDate = SimpleDateFormat("MMM d", Locale.getDefault()).format(selectedDate.time)
                        val sectionHeader = when {
                            viewModel.isFilterActive -> stringResource(R.string.search_tasks)
                            else -> stringResource(R.string.tasks_for_date, formattedDate)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sectionHeader,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                                    color = textPrimaryFor(isDark)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Accent.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = "${displayedTasks.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = Accent,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Task Items or Clean Empty State
                        if (displayedTasks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp, horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (viewModel.isFilterActive) "🔍" else "✦",
                                        fontSize = 22.sp,
                                        color = Accent
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val emptyTitle = when {
                                        viewModel.isFilterActive && viewModel.selectedPriorityFilter != null && viewModel.searchQuery.isBlank() && viewModel.selectedTagFilter == null -> {
                                            val pName = when (viewModel.selectedPriorityFilter!!) {
                                                com.example.mytodoapp.model.Priority.HIGH -> stringResource(R.string.priority_high)
                                                com.example.mytodoapp.model.Priority.MEDIUM -> stringResource(R.string.priority_medium)
                                                com.example.mytodoapp.model.Priority.LOW -> stringResource(R.string.priority_low)
                                            }
                                            stringResource(R.string.no_priority_tasks_found, pName)
                                        }
                                        viewModel.isFilterActive && !viewModel.selectedTagFilter.isNullOrBlank() && viewModel.searchQuery.isBlank() -> {
                                            stringResource(R.string.no_tasks_match_tag)
                                        }
                                        viewModel.isFilterActive -> {
                                            stringResource(R.string.no_tasks_match_search)
                                        }
                                        else -> {
                                            stringResource(R.string.no_tasks_found)
                                        }
                                    }
                                    Text(
                                        text = emptyTitle,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                                        color = textPrimaryFor(isDark),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (viewModel.isFilterActive) {
                                            stringResource(R.string.no_matching_tasks_msg)
                                        } else {
                                            stringResource(R.string.no_tasks_today_msg)
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = textSecondaryFor(isDark),
                                        textAlign = TextAlign.Center
                                    )

                                    if (viewModel.isFilterActive) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.clearFilters() },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Accent),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.clear_filters),
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                displayedTasks.forEach { todo ->
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
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "percentage"
    )
    val animatedIntPercentage = animateIntAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "intPercentage"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val isDark = LocalIsDarkTheme.current
        val ringTrack = if (isDark) Color(0xFF2E2E2E) else Color(0xFFE2E8F0)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
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

        // Percentage Text in center (Smooth count animation)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${animatedIntPercentage.value}%",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = textPrimaryFor(isDark)
            )
            Text(
                text = "Done",
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
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                color = textMutedFor(isDark)
            )
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
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
            .height(110.dp)
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
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(if (isDark) Color(0xFF2E2E2E) else Color(0xFFE2E8F0))
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
                                .height(6.dp)
                                .width(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isDark) Color(0xFF262626) else Color(0xFFEEF0F3))
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
fun MonthlyBarChart(
    data: List<MonthlyChartData>,
    isDark: Boolean
) {
    val maxTasks = data.maxOf { it.completedCount + it.pendingCount }.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
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
                                .width(15.dp)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(if (isDark) Color(0xFF2E2E2E) else Color(0xFFE2E8F0))
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
                                .height(6.dp)
                                .width(15.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isDark) Color(0xFF262626) else Color(0xFFEEF0F3))
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