package com.example.mytodoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.model.Todo
import com.example.mytodoapp.ui.*

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    todo: Todo,
    onBack: () -> Unit,
    isDark: Boolean,
    eligibleSwapTasks: List<Todo> = emptyList(),
    onConfirmSwap: (Todo) -> Unit = {}
) {
    var showFullPreview by remember { mutableStateOf(false) }
    var showSwapPicker by remember { mutableStateOf(false) }
    var swapTarget by remember { mutableStateOf<Todo?>(null) }
    val canSwapTime = !todo.completed && todo.dueTimeMillis != null && todo.endTimeMillis != null

    Scaffold(
        containerColor = backgroundColorFor(isDark),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Task Details",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimaryFor(isDark)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimaryFor(isDark)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColorFor(isDark),
                    titleContentColor = textPrimaryFor(isDark)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Task Card (bundles Title, Description, Status Pill, and Priority Badge together)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Pill & Priority Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Accent.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (todo.completed) "Completed" else "Pending",
                                color = Accent,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Priority Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PriorityDot(priority = todo.priority)
                            Spacer(modifier = Modifier.width(6.dp))
                            val priorityText = when (todo.priority) {
                                Priority.HIGH -> "High Priority"
                                Priority.MEDIUM -> "Medium Priority"
                                Priority.LOW -> "Low Priority"
                            }
                            Text(
                                text = priorityText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = textSecondaryFor(isDark)
                            )
                        }
                    }

                    // Task Title
                    Text(
                        text = todo.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimaryFor(isDark)
                    )

                    // Description (if present)
                    if (todo.description.isNotBlank()) {
                        HorizontalDivider(color = Accent.copy(alpha = 0.2f), thickness = 1.dp)
                        Column {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = textSecondaryFor(isDark)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = todo.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textPrimaryFor(isDark)
                            )
                        }
                    }
                }
            }

            // Date & Time Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Date & Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = textSecondaryFor(isDark),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                                .format(Date(todo.dueTimeMillis ?: todo.createdAt))
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textPrimaryFor(isDark)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Accent.copy(alpha = 0.35f))
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "Start Time",
                                style = MaterialTheme.typography.labelMedium,
                                color = textSecondaryFor(isDark),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                .format(Date(todo.dueTimeMillis ?: todo.createdAt))
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textPrimaryFor(isDark)
                            )
                        }
                    }

                    if (todo.endTimeMillis != null) {
                        HorizontalDivider(color = Accent.copy(alpha = 0.2f), thickness = 1.dp)

                        // Due Date & Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Due Date",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textSecondaryFor(isDark),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                                    .format(Date(todo.endTimeMillis))
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textPrimaryFor(isDark)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(Accent.copy(alpha = 0.35f))
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = "Due Time",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textSecondaryFor(isDark),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                    .format(Date(todo.endTimeMillis))
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textPrimaryFor(isDark)
                                )
                            }
                        }
                    }

                    if (canSwapTime) {
                        HorizontalDivider(color = Accent.copy(alpha = 0.2f), thickness = 1.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            SwapTimeActionButton(
                                onClick = { showSwapPicker = true }
                            )
                        }
                    }
                }
            }

            // Attached Image Card Section
            if (!todo.attachmentUri.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                    border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Attachment",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = textSecondaryFor(isDark)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showFullPreview = true },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = todo.attachmentUri,
                                contentDescription = "Attachment preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSwapPicker) {
        SwapTimePickerSheet(
            isDark = isDark,
            candidates = eligibleSwapTasks,
            onSelect = { selected ->
                swapTarget = selected
                showSwapPicker = false
            },
            onDismiss = { showSwapPicker = false }
        )
    }

    swapTarget?.let { other ->
        SwapTimeConfirmDialog(
            isDark = isDark,
            source = todo,
            other = other,
            onConfirm = {
                val selected = other
                swapTarget = null
                onConfirmSwap(selected)
            },
            onDismiss = { swapTarget = null }
        )
    }

    if (showFullPreview && !todo.attachmentUri.isNullOrEmpty()) {
        Dialog(onDismissRequest = { showFullPreview = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = surfaceColorFor(isDark),
                border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = todo.attachmentUri,
                        contentDescription = "Full attachment preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showFullPreview = false }) {
                        Text("Close", color = Accent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
