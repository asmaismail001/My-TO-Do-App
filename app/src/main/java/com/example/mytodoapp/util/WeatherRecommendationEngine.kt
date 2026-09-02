package com.example.mytodoapp.util

import com.example.mytodoapp.model.TaskType
import com.example.mytodoapp.model.WeatherCondition
import com.example.mytodoapp.model.WeatherIconType
import com.example.mytodoapp.model.WeatherRecommendation
import com.example.mytodoapp.model.WeatherRecommendationLevel

object WeatherRecommendationEngine {

    // Centralized Thresholds Configuration
    object Thresholds {
        const val MAX_COMFORT_TEMP_C = 38.0
        const val MIN_COMFORT_TEMP_C = 2.0
        const val HIGH_RAIN_PROBABILITY_PERCENT = 50
        const val MODERATE_RAIN_PROBABILITY_PERCENT = 25
        const val HIGH_WIND_SPEED_KMH = 38.0
        const val MODERATE_WIND_SPEED_KMH = 25.0
    }

    fun evaluateRecommendation(
        temperatureC: Double,
        rainProbability: Int,
        windSpeedKmh: Double,
        condition: WeatherCondition,
        taskType: TaskType = TaskType.FLEXIBLE
    ): WeatherRecommendation {

        // For indoor tasks: provide informative context without alarming warning
        if (taskType == TaskType.INDOOR) {
            val shortDesc = "${condition.title}, ${Math.round(temperatureC)}°C"
            return WeatherRecommendation(
                level = WeatherRecommendationLevel.GOOD_FOR_OUTDOOR,
                badgeTitle = "Indoor Task",
                message = "Indoor task: Weather won't affect your activity ($shortDesc outside).",
                reason = "Task is set to Indoor.",
                isFavorableForOutdoor = true
            )
        }

        // 1. Check for extreme temperatures
        if (temperatureC > Thresholds.MAX_COMFORT_TEMP_C) {
            return WeatherRecommendation(
                level = WeatherRecommendationLevel.EXTREME_WEATHER,
                badgeTitle = "Extreme Heat",
                message = "Temperature conditions may be uncomfortable for an outdoor activity (${Math.round(temperatureC)}°C).",
                reason = "High temperature exceeding ${Thresholds.MAX_COMFORT_TEMP_C.toInt()}°C.",
                isFavorableForOutdoor = false
            )
        }
        if (temperatureC < Thresholds.MIN_COMFORT_TEMP_C) {
            return WeatherRecommendation(
                level = WeatherRecommendationLevel.EXTREME_WEATHER,
                badgeTitle = "Freezing / Low Temp",
                message = "Temperature conditions may be uncomfortable for an outdoor activity (${Math.round(temperatureC)}°C).",
                reason = "Low temperature below ${Thresholds.MIN_COMFORT_TEMP_C.toInt()}°C.",
                isFavorableForOutdoor = false
            )
        }

        // 2. Check for severe weather conditions (thunderstorm, heavy rain, snow)
        if (condition.iconType == WeatherIconType.THUNDERSTORM ||
            condition.iconType == WeatherIconType.HEAVY_RAIN ||
            condition.iconType == WeatherIconType.SNOW
        ) {
            return WeatherRecommendation(
                level = WeatherRecommendationLevel.BETTER_INDOOR,
                badgeTitle = "Better Indoor",
                message = "Weather conditions may not be suitable for an outdoor task (${condition.title.lowercase()}).",
                reason = condition.description,
                isFavorableForOutdoor = false
            )
        }

        // 3. Check for high rain probability or active rain
        if (rainProbability >= Thresholds.HIGH_RAIN_PROBABILITY_PERCENT || condition.iconType == WeatherIconType.RAIN) {
            return WeatherRecommendation(
                level = WeatherRecommendationLevel.RAIN_EXPECTED,
                badgeTitle = "Rain Expected",
                message = "Rain is likely ($rainProbability% chance). Consider moving this task indoors or rescheduling it.",
                reason = "High rain probability of $rainProbability%.",
                isFavorableForOutdoor = false
            )
        }

        // 4. Check for moderate weather risks (moderate rain risk, high wind, fog)
        if (rainProbability >= Thresholds.MODERATE_RAIN_PROBABILITY_PERCENT) {
            return WeatherRecommendation(
                level = WeatherRecommendationLevel.MAYBE_OUTDOOR,
                badgeTitle = "Maybe Outdoor",
                message = "Conditions are acceptable, but check the weather before starting ($rainProbability% chance of rain).",
                reason = "Moderate chance of precipitation.",
                isFavorableForOutdoor = true
            )
        }

        if (windSpeedKmh >= Thresholds.HIGH_WIND_SPEED_KMH) {
            return WeatherRecommendation(
                level = WeatherRecommendationLevel.MAYBE_OUTDOOR,
                badgeTitle = "Windy Conditions",
                message = "Conditions are acceptable, but check the weather before starting (wind ${windSpeedKmh.toInt()} km/h).",
                reason = "Elevated wind speed.",
                isFavorableForOutdoor = true
            )
        }

        if (condition.iconType == WeatherIconType.FOG) {
            return WeatherRecommendation(
                level = WeatherRecommendationLevel.MAYBE_OUTDOOR,
                badgeTitle = "Misty / Foggy",
                message = "Conditions are acceptable, but check visibility before outdoor activities.",
                reason = "Foggy conditions.",
                isFavorableForOutdoor = true
            )
        }

        // 5. Default good outdoor conditions
        return WeatherRecommendation(
            level = WeatherRecommendationLevel.GOOD_FOR_OUTDOOR,
            badgeTitle = "Good for Outdoor",
            message = "Weather looks good for an outdoor task.",
            reason = "Clear/favorable conditions (${condition.title}, ${Math.round(temperatureC)}°C).",
            isFavorableForOutdoor = true
        )
    }
}
