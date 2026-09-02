package com.example.mytodoapp.model

enum class WeatherIconType {
    SUNNY,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    HEAVY_RAIN,
    THUNDERSTORM,
    SNOW,
    WINDY
}

data class WeatherCondition(
    val code: Int,
    val title: String,
    val description: String,
    val iconType: WeatherIconType
) {
    companion object {
        fun fromWmoCode(code: Int): WeatherCondition {
            return when (code) {
                0 -> WeatherCondition(code, "Clear Sky", "Sunny and clear", WeatherIconType.SUNNY)
                1 -> WeatherCondition(code, "Mainly Clear", "Mostly clear skies", WeatherIconType.SUNNY)
                2 -> WeatherCondition(code, "Partly Cloudy", "Scattered clouds", WeatherIconType.PARTLY_CLOUDY)
                3 -> WeatherCondition(code, "Overcast", "Cloudy skies", WeatherIconType.CLOUDY)
                45, 48 -> WeatherCondition(code, "Foggy", "Misty or foggy conditions", WeatherIconType.FOG)
                51, 53, 55 -> WeatherCondition(code, "Drizzle", "Light drizzle", WeatherIconType.DRIZZLE)
                56, 57 -> WeatherCondition(code, "Freezing Drizzle", "Cold freezing drizzle", WeatherIconType.DRIZZLE)
                61 -> WeatherCondition(code, "Light Rain", "Passing light rain", WeatherIconType.RAIN)
                63 -> WeatherCondition(code, "Moderate Rain", "Steady rainfall", WeatherIconType.RAIN)
                65 -> WeatherCondition(code, "Heavy Rain", "Heavy downpour", WeatherIconType.HEAVY_RAIN)
                66, 67 -> WeatherCondition(code, "Freezing Rain", "Icy freezing rain", WeatherIconType.HEAVY_RAIN)
                71, 73, 75, 77 -> WeatherCondition(code, "Snow", "Snowfall expected", WeatherIconType.SNOW)
                80, 81 -> WeatherCondition(code, "Rain Showers", "Scattered rain showers", WeatherIconType.RAIN)
                82 -> WeatherCondition(code, "Violent Rain", "Intense torrential showers", WeatherIconType.HEAVY_RAIN)
                85, 86 -> WeatherCondition(code, "Snow Showers", "Snow flurries", WeatherIconType.SNOW)
                95 -> WeatherCondition(code, "Thunderstorm", "Thunderstorm active", WeatherIconType.THUNDERSTORM)
                96, 99 -> WeatherCondition(code, "Severe Storm", "Thunderstorm with hail", WeatherIconType.THUNDERSTORM)
                else -> WeatherCondition(code, "Clear", "Normal weather", WeatherIconType.SUNNY)
            }
        }
    }
}

enum class WeatherRecommendationLevel {
    GOOD_FOR_OUTDOOR,
    MAYBE_OUTDOOR,
    BETTER_INDOOR,
    RAIN_EXPECTED,
    EXTREME_WEATHER
}

data class WeatherRecommendation(
    val level: WeatherRecommendationLevel,
    val badgeTitle: String,
    val message: String,
    val reason: String = "",
    val isFavorableForOutdoor: Boolean = true
)

data class WeatherData(
    val timestampMillis: Long,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val rainProbability: Int,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val condition: WeatherCondition,
    val recommendation: WeatherRecommendation,
    val locationName: String = "Current Location"
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val isManual: Boolean = false
)

sealed interface WeatherUiState {
    object Idle : WeatherUiState
    object Loading : WeatherUiState
    data class Success(val data: WeatherData) : WeatherUiState
    object ForecastUnavailable : WeatherUiState
    data class Error(val message: String) : WeatherUiState
    object LocationPermissionRequired : WeatherUiState
}
