package com.example.mytodoapp.repository.remote.weather.dto

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("current") val current: CurrentWeatherDto?,
    @SerializedName("hourly") val hourly: HourlyWeatherDto?
)

data class CurrentWeatherDto(
    @SerializedName("time") val time: String?,
    @SerializedName("temperature_2m") val temperature2m: Double?,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: Int?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("precipitation_probability") val precipitationProbability: Int?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double?
)

data class HourlyWeatherDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m") val temperature2m: List<Double>?,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: List<Int>?,
    @SerializedName("apparent_temperature") val apparentTemperature: List<Double>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("wind_speed_10m") val windSpeed10m: List<Double>?
)
