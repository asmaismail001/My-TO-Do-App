package com.example.mytodoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
import com.example.mytodoapp.model.Priority
import com.example.mytodoapp.ui.*

@Composable
fun TodoSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.search_hint),
    selectedPriority: Priority? = null,
    onPrioritySelect: ((Priority?) -> Unit)? = null,
    selectedTag: String? = null,
    onTagSelect: ((String?) -> Unit)? = null,
    availableTags: List<String> = emptyList(),
    onClearFilters: (() -> Unit)? = null,
    showFiltersRow: Boolean = true
) {
    val isDark = LocalIsDarkTheme.current
    val isAnyFilterActive = query.isNotBlank() || selectedPriority != null || !selectedTag.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Compact modern search bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = searchBarBackgroundFor(isDark),
            border = BorderStroke(1.dp, cardBorderColorFor(isDark))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_tasks),
                    tint = Accent,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = textMutedFor(isDark),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            maxLines = 1
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = textPrimaryFor(isDark),
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = textSecondaryFor(isDark),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Priority and Tag Filter Chips Row
        if (showFiltersRow && (onPrioritySelect != null || onTagSelect != null)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onPrioritySelect != null) {
                    // "All" chip
                    val isAllSelected = selectedPriority == null && selectedTag == null
                    PriorityFilterChip(
                        label = stringResource(R.string.filter_all),
                        isSelected = isAllSelected,
                        dotColor = null,
                        isDark = isDark,
                        onClick = {
                            onPrioritySelect(null)
                            onTagSelect?.invoke(null)
                        }
                    )

                    // Priority Chips
                    PriorityFilterChip(
                        label = stringResource(R.string.priority_high),
                        isSelected = selectedPriority == Priority.HIGH,
                        dotColor = PriorityHigh,
                        isDark = isDark,
                        onClick = { onPrioritySelect(if (selectedPriority == Priority.HIGH) null else Priority.HIGH) }
                    )

                    PriorityFilterChip(
                        label = stringResource(R.string.priority_medium),
                        isSelected = selectedPriority == Priority.MEDIUM,
                        dotColor = PriorityMedium,
                        isDark = isDark,
                        onClick = { onPrioritySelect(if (selectedPriority == Priority.MEDIUM) null else Priority.MEDIUM) }
                    )

                    PriorityFilterChip(
                        label = stringResource(R.string.priority_low),
                        isSelected = selectedPriority == Priority.LOW,
                        dotColor = PriorityLow,
                        isDark = isDark,
                        onClick = { onPrioritySelect(if (selectedPriority == Priority.LOW) null else Priority.LOW) }
                    )
                }

                // Divider before tags if tags exist
                if (availableTags.isNotEmpty() && onTagSelect != null) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(cardBorderColorFor(isDark))
                    )

                    availableTags.forEach { tag ->
                        val isTagSelected = selectedTag.equals(tag, ignoreCase = true)
                        TagChip(
                            tag = tag,
                            isDark = isDark,
                            isSelected = isTagSelected,
                            onClick = {
                                onTagSelect(if (isTagSelected) null else tag)
                            }
                        )
                    }
                }

                // Reset / Clear Filters Chip
                AnimatedVisibility(
                    visible = isAnyFilterActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DeleteRed.copy(alpha = if (isDark) 0.2f else 0.1f),
                        border = BorderStroke(0.8.dp, DeleteRed.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onClearFilters?.invoke() ?: run {
                                    onQueryChange("")
                                    onPrioritySelect?.invoke(null)
                                    onTagSelect?.invoke(null)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear_filters),
                                tint = DeleteRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.clear_filters),
                                color = DeleteRed,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityFilterChip(
    label: String,
    isSelected: Boolean,
    dotColor: Color?,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Accent else surfaceColorFor(isDark)
    val textColor = if (isSelected) Color.White else textSecondaryFor(isDark)
    val borderColor = if (isSelected) Accent else cardBorderColorFor(isDark)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else dotColor)
                )
            }
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp
                )
            )
        }
    }
}