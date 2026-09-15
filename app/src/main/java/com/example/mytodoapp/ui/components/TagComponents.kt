package com.example.mytodoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.R
import com.example.mytodoapp.ui.Accent
import com.example.mytodoapp.ui.cardBorderColorFor
import com.example.mytodoapp.ui.surfaceColorFor
import com.example.mytodoapp.ui.textMutedFor
import com.example.mytodoapp.ui.textPrimaryFor
import com.example.mytodoapp.ui.textSecondaryFor

@Composable
fun TagChip(
    tag: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    val bgColor = when {
        isSelected -> Accent
        onRemove != null -> Accent.copy(alpha = if (isDark) 0.22f else 0.12f)
        else -> if (isDark) Color(0xFF1E293B) else Color(0xFFE6F3F3)
    }

    val textColor = when {
        isSelected -> Color.White
        else -> Accent
    }

    val borderColor = when {
        isSelected -> Accent
        else -> Accent.copy(alpha = 0.35f)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(0.8.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "#$tag",
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            )

            if (onRemove != null) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.delete),
                        tint = textColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TagInputSection(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var customTagText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val presetTags = remember {
        listOf("Work", "Study", "Personal", "Meeting", "Urgent", "Shopping", "Project")
    }

    val addTag: (String) -> Unit = { rawTag ->
        val trimmed = rawTag.trim().removePrefix("#")
        if (trimmed.isNotBlank() && !tags.any { it.equals(trimmed, ignoreCase = true) }) {
            onTagsChange(tags + trimmed)
        }
    }

    val removeTag: (String) -> Unit = { tagToRemove ->
        onTagsChange(tags.filterNot { it.equals(tagToRemove, ignoreCase = true) })
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.tags),
                style = MaterialTheme.typography.labelMedium,
                color = textSecondaryFor(isDark),
                fontWeight = FontWeight.SemiBold
            )
            if (tags.isNotEmpty()) {
                Text(
                    text = "${tags.size} selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Selected tags list
        if (tags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        isDark = isDark,
                        onRemove = { removeTag(tag) }
                    )
                }
            }
        }

        // Preset tag suggestions horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetTags.forEach { preset ->
                val isSelected = tags.any { it.equals(preset, ignoreCase = true) }
                TagChip(
                    tag = preset,
                    isDark = isDark,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) {
                            removeTag(preset)
                        } else {
                            addTag(preset)
                        }
                    }
                )
            }
        }

        // Custom Tag Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customTagText,
                onValueChange = { customTagText = it },
                placeholder = {
                    Text(
                        stringResource(R.string.enter_tag),
                        color = textMutedFor(isDark),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Accent.copy(alpha = 0.3f),
                    focusedTextColor = textPrimaryFor(isDark),
                    unfocusedTextColor = textPrimaryFor(isDark),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (customTagText.isNotBlank()) {
                            addTag(customTagText)
                            customTagText = ""
                        }
                        keyboardController?.hide()
                    }
                )
            )

            Button(
                onClick = {
                    if (customTagText.isNotBlank()) {
                        addTag(customTagText)
                        customTagText = ""
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier.height(48.dp),
                enabled = customTagText.trim().isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_tag),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.add_tag),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun RecurrenceSelector(
    selected: com.example.mytodoapp.model.RecurrenceType,
    onSelect: (com.example.mytodoapp.model.RecurrenceType) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        com.example.mytodoapp.model.RecurrenceType.NONE to stringResource(R.string.does_not_repeat),
        com.example.mytodoapp.model.RecurrenceType.DAILY to stringResource(R.string.every_day),
        com.example.mytodoapp.model.RecurrenceType.WEEKLY to stringResource(R.string.every_week),
        com.example.mytodoapp.model.RecurrenceType.MONTHLY to stringResource(R.string.every_month)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.repeat),
                style = MaterialTheme.typography.labelMedium,
                color = textSecondaryFor(isDark),
                fontWeight = FontWeight.SemiBold
            )
            if (selected != com.example.mytodoapp.model.RecurrenceType.NONE) {
                Text(
                    text = when (selected) {
                        com.example.mytodoapp.model.RecurrenceType.DAILY -> stringResource(R.string.repeats_daily)
                        com.example.mytodoapp.model.RecurrenceType.WEEKLY -> stringResource(R.string.repeats_weekly)
                        com.example.mytodoapp.model.RecurrenceType.MONTHLY -> stringResource(R.string.repeats_monthly)
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { (type, label) ->
                val isSelected = selected == type
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Accent else (if (isDark) Color(0xFF1E293B) else Color(0xFFE6F3F3)),
                    border = BorderStroke(1.dp, if (isSelected) Accent else cardBorderColorFor(isDark)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(type) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (type == com.example.mytodoapp.model.RecurrenceType.DAILY) {
                            Text(text = "🔁", fontSize = 12.sp)
                        }
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else textPrimaryFor(isDark),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
