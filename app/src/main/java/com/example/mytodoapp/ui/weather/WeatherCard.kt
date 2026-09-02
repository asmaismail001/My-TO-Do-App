package com.example.mytodoapp.ui.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Thermostat
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

@Composable
fun WeatherCard(
    weatherUiState: WeatherUiState,
    taskType: TaskType = TaskType.FLEXIBLE,
    onRefresh: () -> Unit = {},
    onRescheduleClick: (() -> Unit)? = null,
    onMarkAsIndoorClick: (() -> Unit)? = null,
    onKeepTaskClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String = "Weather at Task Time"
) {
    val isDark = LocalIsDarkTheme.current

    Card(
        modifier = modifier.fillMaxWidth(),
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌤 $title",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryFor(isDark)
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh weather",
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            when (weatherUiState) {
                is WeatherUiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Accent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Checking weather forecast...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondaryFor(isDark)
                        )
                    }
                }

                is WeatherUiState.Success -> {
                    val weather = weatherUiState.data
                    val rec = weather.recommendation

                    // Temperature and Condition Main Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WeatherIcon(
                                iconType = weather.condition.iconType,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${Math.round(weather.temperatureC)}°C",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = textPrimaryFor(isDark)
                                )
                                Text(
                                    text = "Feels like ${Math.round(weather.feelsLikeC)}°C",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMutedFor(isDark)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = weather.condition.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = textPrimaryFor(isDark)
                            )
                            Text(
                                text = weather.locationName,
                                style = MaterialTheme.typography.labelSmall,
                                color = textMutedFor(isDark)
                            )
                        }
                    }

                    HorizontalDivider(color = Accent.copy(alpha = 0.15f), thickness = 1.dp)

                    // Secondary Metrics (Rain %, Wind, Humidity)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rain Probability
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.WaterDrop,
                                contentDescription = null,
                                tint = WeatherRainBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rain: ${weather.rainProbability}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryFor(isDark)
                            )
                        }

                        // Wind Speed
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Air,
                                contentDescription = null,
                                tint = textMutedFor(isDark),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wind: ${weather.windSpeedKmh.toInt()} km/h",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryFor(isDark)
                            )
                        }

                        // Humidity
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Thermostat,
                                contentDescription = null,
                                tint = textMutedFor(isDark),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Humidity: ${weather.humidityPercent}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryFor(isDark)
                            )
                        }
                    }

                    // Recommendation Pill & Advice
                    val (badgeBg, badgeText, badgeIcon) = when (rec.level) {
                        WeatherRecommendationLevel.GOOD_FOR_OUTDOOR -> Triple(
                            Accent.copy(alpha = 0.15f),
                            Accent,
                            Icons.Outlined.CheckCircle
                        )
                        WeatherRecommendationLevel.MAYBE_OUTDOOR -> Triple(
                            Color(0xFFFEF3C7),
                            Color(0xFFB45309),
                            Icons.Outlined.Info
                        )
                        WeatherRecommendationLevel.BETTER_INDOOR -> Triple(
                            Color(0xFFFEE2E2),
                            Color(0xFFEF4444),
                            Icons.Outlined.WarningAmber
                        )
                        WeatherRecommendationLevel.RAIN_EXPECTED -> Triple(
                            Color(0xFFE0F2FE),
                            Color(0xFF0284C7),
                            Icons.Outlined.WaterDrop
                        )
                        WeatherRecommendationLevel.EXTREME_WEATHER -> Triple(
                            Color(0xFFFEE2E2),
                            Color(0xFFDC2626),
                            Icons.Outlined.WarningAmber
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(badgeBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = null,
                                tint = badgeText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = rec.message,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = badgeText
                            )
                        }
                    }

                    // Optional Action buttons for bad weather
                    if (!rec.isFavorableForOutdoor && taskType != TaskType.INDOOR && onRescheduleClick != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onMarkAsIndoorClick != null) {
                                OutlinedButton(
                                    onClick = onMarkAsIndoorClick,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Accent.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Home,
                                        contentDescription = null,
                                        tint = Accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mark Indoor",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = onRescheduleClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reschedule",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                is WeatherUiState.ForecastUnavailable -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = textMutedFor(isDark),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Weather forecast is not available for this date yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMutedFor(isDark)
                        )
                    }
                }

                is WeatherUiState.LocationPermissionRequired -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = PriorityMedium,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Location permission is required to provide weather-based task suggestions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryFor(isDark)
                        )
                    }
                }

                is WeatherUiState.Error -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = DeleteRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = weatherUiState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryFor(isDark)
                        )
                    }
                }

                is WeatherUiState.Idle -> {
                    Text(
                        text = "Set a task date and time to view weather recommendations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedFor(isDark)
                    )
                }
            }
        }
    }
}
