package com.example.mytodoapp.ui.weather

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mytodoapp.model.WeatherIconType

val WeatherYellow = Color(0xFFF59E0B)
val WeatherCloudBlue = Color(0xFF64748B)
val WeatherRainBlue = Color(0xFF0284C7)
val WeatherThunderPurple = Color(0xFF7C3AED)
val WeatherSnowCyan = Color(0xFF06B6D4)

@Composable
fun WeatherIcon(
    iconType: WeatherIconType,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    overrideTint: Color? = null
) {
    val (vector, tint) = when (iconType) {
        WeatherIconType.SUNNY -> Icons.Outlined.WbSunny to WeatherYellow
        WeatherIconType.PARTLY_CLOUDY -> Icons.Outlined.CloudQueue to Color(0xFFEAB308)
        WeatherIconType.CLOUDY -> Icons.Outlined.Cloud to WeatherCloudBlue
        WeatherIconType.FOG -> Icons.Outlined.Air to WeatherCloudBlue
        WeatherIconType.DRIZZLE -> Icons.Outlined.Grain to WeatherRainBlue
        WeatherIconType.RAIN -> Icons.Outlined.WaterDrop to WeatherRainBlue
        WeatherIconType.HEAVY_RAIN -> Icons.Outlined.WaterDrop to Color(0xFF1D4ED8)
        WeatherIconType.THUNDERSTORM -> Icons.Outlined.Thunderstorm to WeatherThunderPurple
        WeatherIconType.SNOW -> Icons.Outlined.AcUnit to WeatherSnowCyan
        WeatherIconType.WINDY -> Icons.Outlined.Air to WeatherCloudBlue
    }

    Icon(
        imageVector = vector,
        contentDescription = contentDescription,
        tint = overrideTint ?: tint,
        modifier = modifier
    )
}
