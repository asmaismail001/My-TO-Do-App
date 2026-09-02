package com.example.mytodoapp.ui.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mytodoapp.model.*
import com.example.mytodoapp.ui.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherDetailsDialog(
    weatherUiState: WeatherUiState,
    locationData: LocationData,
    hourlyForecast: List<WeatherData>,
    onChangeLocationClick: () -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val hourFormat = SimpleDateFormat("h a", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = surfaceColorFor(isDark),
            border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f)),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Weather Forecast",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryFor(isDark)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = locationData.locationName,
                                style = MaterialTheme.typography.labelMedium,
                                color = Accent,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = Accent
                        )
                    }
                }

                when (weatherUiState) {
                    is WeatherUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Accent)
                        }
                    }

                    is WeatherUiState.Success -> {
                        val weather = weatherUiState.data

                        // Current Weather Main Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8F9FA)),
                            border = BorderStroke(1.dp, Accent.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        WeatherIcon(
                                            iconType = weather.condition.iconType,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${Math.round(weather.temperatureC)}°C",
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontWeight = FontWeight.Bold
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
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = textPrimaryFor(isDark)
                                        )
                                        Text(
                                            text = weather.condition.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecondaryFor(isDark)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Accent.copy(alpha = 0.15f))

                                // Detailed Metrics Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MetricItem(
                                        icon = Icons.Outlined.WaterDrop,
                                        label = "Rain",
                                        value = "${weather.rainProbability}%",
                                        tint = WeatherRainBlue,
                                        isDark = isDark
                                    )
                                    MetricItem(
                                        icon = Icons.Outlined.Air,
                                        label = "Wind",
                                        value = "${weather.windSpeedKmh.toInt()} km/h",
                                        tint = Accent,
                                        isDark = isDark
                                    )
                                    MetricItem(
                                        icon = Icons.Outlined.Thermostat,
                                        label = "Humidity",
                                        value = "${weather.humidityPercent}%",
                                        tint = WeatherYellow,
                                        isDark = isDark
                                    )
                                }
                            }
                        }

                        // Hourly Forecast Section (Next 24 Hours)
                        if (hourlyForecast.isNotEmpty()) {
                            Text(
                                text = "Next 24 Hours",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryFor(isDark)
                            )

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(hourlyForecast) { item ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                        ),
                                        border = BorderStroke(1.dp, Accent.copy(alpha = 0.15f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = hourFormat.format(Date(item.timestampMillis)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textMutedFor(isDark)
                                            )
                                            WeatherIcon(
                                                iconType = item.condition.iconType,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                text = "${Math.round(item.temperatureC)}°",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = textPrimaryFor(isDark)
                                            )
                                            if (item.rainProbability > 0) {
                                                Text(
                                                    text = "${item.rainProbability}%",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = WeatherRainBlue,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Recommendation Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Accent.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = Accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = weather.recommendation.badgeTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Accent
                                    )
                                }
                                Text(
                                    text = weather.recommendation.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textPrimaryFor(isDark)
                                )
                            }
                        }
                    }

                    is WeatherUiState.Error -> {
                        Text(
                            text = weatherUiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeleteRed
                        )
                    }

                    else -> {}
                }

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onChangeLocationClick) {
                        Icon(
                            imageVector = Icons.Outlined.EditLocation,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Location", color = Accent, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    isDark: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = textMutedFor(isDark))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = textPrimaryFor(isDark)
        )
    }
}
