package com.example.mytodoapp.repository.remote.weather.dto

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("results") val results: List<GeocodingResultDto>?
)

data class GeocodingResultDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String?,
    @SerializedName("country_code") val countryCode: String?,
    @SerializedName("admin1") val admin1: String?
)
