package com.example.mytodoapp.ui.components

import androidx.compose.foundation.BorderStroke
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
    isDark: Boolean
) {
    var showFullPreview by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Task Title
            Text(
                text = todo.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = textPrimaryFor(isDark)
            )

            // Description
            if (todo.description.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                    border = BorderStroke(1.dp, cardBorderColorFor(isDark))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textSecondaryFor(isDark)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textPrimaryFor(isDark)
                        )
                    }
                }
            }

            // Meta Info (Date, Time, Priority, Status)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
                border = BorderStroke(1.dp, cardBorderColorFor(isDark))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date & Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date",
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

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Time",
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

                    HorizontalDivider(color = cardBorderColorFor(isDark), thickness = 1.dp)

                    // Priority & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Priority",
                                style = MaterialTheme.typography.labelMedium,
                                color = textSecondaryFor(isDark),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PriorityDot(priority = todo.priority)
                                Spacer(modifier = Modifier.width(6.dp))
                                val priorityText = when (todo.priority) {
                                    Priority.HIGH -> "High"
                                    Priority.MEDIUM -> "Medium"
                                    Priority.LOW -> "Low"
                                }
                                Text(
                                    text = priorityText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textPrimaryFor(isDark)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.labelMedium,
                                color = textSecondaryFor(isDark),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (todo.completed) "Completed" else "Pending",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (todo.completed) SuccessGreen else Accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Attached Image Section
            if (!todo.attachmentUri.isNullOrEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Attachment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textSecondaryFor(isDark)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clickable { showFullPreview = true },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, cardBorderColorFor(isDark)),
                        colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = todo.attachmentUri,
                                contentDescription = "Attachment preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFullPreview && !todo.attachmentUri.isNullOrEmpty()) {
        Dialog(onDismissRequest = { showFullPreview = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = surfaceColorFor(isDark),
                border = BorderStroke(1.dp, cardBorderColorFor(isDark))
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
