package com.example.mytodoapp.repository.remote.weather

import com.example.mytodoapp.repository.remote.weather.dto.GeocodingResponse
import com.example.mytodoapp.repository.remote.weather.dto.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = WeatherApiConfig.DEFAULT_CURRENT_PARAMS,
        @Query("hourly") hourly: String = WeatherApiConfig.DEFAULT_HOURLY_PARAMS,
        @Query("timezone") timezone: String = WeatherApiConfig.DEFAULT_TIMEZONE,
        @Query("forecast_days") forecastDays: Int = WeatherApiConfig.DEFAULT_FORECAST_DAYS
    ): OpenMeteoResponse

    @GET
    suspend fun searchLocations(
        @Url url: String,
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}
