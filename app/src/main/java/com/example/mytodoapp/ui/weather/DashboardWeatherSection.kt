package com.example.mytodoapp.ui.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodoapp.model.*
import com.example.mytodoapp.ui.*
import com.example.mytodoapp.viewmodel.OutdoorTasksSummary

@Composable
fun DashboardWeatherSection(
    weatherUiState: WeatherUiState,
    locationData: LocationData,
    outdoorSummary: OutdoorTasksSummary,
    onClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColorFor(isDark)),
        border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Location Name + Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = locationData.locationName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Accent,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMutedFor(isDark)
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "View weather details",
                        tint = textMutedFor(isDark),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            when (weatherUiState) {
                is WeatherUiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Accent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Updating weather...",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryFor(isDark)
                        )
                    }
                }

                is WeatherUiState.Success -> {
                    val weather = weatherUiState.data

                    // Weather Overview Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WeatherIcon(
                                iconType = weather.condition.iconType,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${Math.round(weather.temperatureC)}°C",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = textPrimaryFor(isDark)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = weather.condition.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = textSecondaryFor(isDark)
                            )
                        }

                        // Rain probability indicator
                        if (weather.rainProbability > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.WaterDrop,
                                    contentDescription = null,
                                    tint = WeatherRainBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${weather.rainProbability}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WeatherRainBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Accent.copy(alpha = 0.15f), thickness = 1.dp)

                    // Outdoor Tasks Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (outdoorSummary.hasBadWeatherAlert) Icons.Outlined.WarningAmber else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (outdoorSummary.hasBadWeatherAlert) PriorityMedium else Accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = outdoorSummary.summaryText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = textSecondaryFor(isDark),
                            maxLines = 1
                        )
                    }
                }

                is WeatherUiState.LocationPermissionRequired -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable location for weather forecast suggestions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryFor(isDark),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is WeatherUiState.Error -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = weatherUiState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryFor(isDark),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Retry", tint = Accent, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                is WeatherUiState.ForecastUnavailable -> {
                    Text(
                        text = "Weather forecast is currently unavailable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedFor(isDark)
                    )
                }

                is WeatherUiState.Idle -> {
                    Text(
                        text = "Tap to check weather forecast.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedFor(isDark)
                    )
                }
            }
        }
    }
}
