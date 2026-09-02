package com.example.mytodoapp.repository.remote.weather

object WeatherApiConfig {
    const val BASE_URL = "https://api.open-meteo.com/"
    const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    
    // Default parameters for Open-Meteo
    const val DEFAULT_CURRENT_PARAMS = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,weather_code,wind_speed_10m"
    const val DEFAULT_HOURLY_PARAMS = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,weather_code,wind_speed_10m"
    const val DEFAULT_TIMEZONE = "auto"
    const val DEFAULT_FORECAST_DAYS = 16
    
    // Cache freshness durations (in milliseconds)
    const val CURRENT_WEATHER_CACHE_TTL_MS = 15 * 60 * 1000L // 15 minutes
    const val FORECAST_CACHE_TTL_MS = 30 * 60 * 1000L // 30 minutes
}
