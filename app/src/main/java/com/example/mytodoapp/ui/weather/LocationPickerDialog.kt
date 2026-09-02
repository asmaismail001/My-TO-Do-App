package com.example.mytodoapp.ui.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mytodoapp.model.LocationData
import com.example.mytodoapp.ui.*

@Composable
fun LocationPickerDialog(
    searchResults: List<LocationData>,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onLocationSelected: (LocationData) -> Unit,
    onUseGpsClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var searchText by remember { mutableStateOf("") }

    val presetLocations = remember {
        listOf(
            LocationData(51.5074, -0.1278, "London, United Kingdom", true),
            LocationData(40.7128, -74.0060, "New York, United States", true),
            LocationData(48.8566, 2.3522, "Paris, France", true),
            LocationData(35.6762, 139.6503, "Tokyo, Japan", true),
            LocationData(25.2048, 55.2708, "Dubai, United Arab Emirates", true),
            LocationData(31.5204, 74.3587, "Lahore, Pakistan", true),
            LocationData(24.8607, 67.0011, "Karachi, Pakistan", true),
            LocationData(41.0082, 28.9784, "Istanbul, Turkey", true),
            LocationData(52.5200, 13.4050, "Berlin, Germany", true),
            LocationData(1.3521, 103.8198, "Singapore", true)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = surfaceColorFor(isDark),
            border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f)),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryFor(isDark)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = textSecondaryFor(isDark)
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        onSearchQueryChange(it)
                    },
                    placeholder = { Text("Search city name...", color = textMutedFor(isDark)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = Accent
                        )
                    },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Accent
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Accent.copy(alpha = 0.35f),
                        focusedTextColor = textPrimaryFor(isDark),
                        unfocusedTextColor = textPrimaryFor(isDark)
                    )
                )

                // Use GPS Location Button
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onUseGpsClick()
                            onDismiss()
                        },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Accent.copy(alpha = 0.4f)),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = Accent.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MyLocation,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Use Device GPS Location",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = textPrimaryFor(isDark)
                            )
                            Text(
                                text = "Auto-detect current coordinates",
                                style = MaterialTheme.typography.labelSmall,
                                color = textSecondaryFor(isDark)
                            )
                        }
                    }
                }

                // Results or Preset Cities List
                val listToShow = if (searchText.trim().length >= 2) searchResults else presetLocations

                Text(
                    text = if (searchText.trim().length >= 2) "Search Results" else "Popular Cities",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondaryFor(isDark)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (listToShow.isEmpty() && searchText.isNotBlank() && !isSearching) {
                        item {
                            Text(
                                text = "No matching cities found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = textMutedFor(isDark),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(listToShow) { location ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onLocationSelected(location)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = location.locationName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textPrimaryFor(isDark),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
