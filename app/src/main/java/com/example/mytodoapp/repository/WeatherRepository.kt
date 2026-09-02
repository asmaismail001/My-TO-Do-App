package com.example.mytodoapp.repository

import com.example.mytodoapp.model.*
import com.example.mytodoapp.repository.remote.weather.WeatherApiConfig
import com.example.mytodoapp.repository.remote.weather.WeatherRetrofitClient
import com.example.mytodoapp.repository.remote.weather.dto.OpenMeteoResponse
import com.example.mytodoapp.util.WeatherRecommendationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class WeatherRepository {

    private val apiService = WeatherRetrofitClient.getService()

    // In-memory cache for coordinate-based weather responses
    private val responseCache = ConcurrentHashMap<String, CachedWeather>()

    private data class CachedWeather(
        val response: OpenMeteoResponse,
        val timestamp: Long
    )

    private fun cacheKey(lat: Double, lon: Double): String {
        // Round to 2 decimal places to reuse nearby cache (~1km)
        val rLat = String.format(Locale.US, "%.2f", lat)
        val rLon = String.format(Locale.US, "%.2f", lon)
        return "$rLat,$rLon"
    }

    suspend fun fetchWeather(
        lat: Double,
        lon: Double,
        forceRefresh: Boolean = false
    ): Result<OpenMeteoResponse> = withContext(Dispatchers.IO) {
        val key = cacheKey(lat, lon)
        val cached = responseCache[key]
        val now = System.currentTimeMillis()

        if (!forceRefresh && cached != null && (now - cached.timestamp) < WeatherApiConfig.FORECAST_CACHE_TTL_MS) {
            return@withContext Result.success(cached.response)
        }

        try {
            val response = apiService.getForecast(latitude = lat, longitude = lon)
            responseCache[key] = CachedWeather(response, now)
            Result.success(response)
        } catch (e: java.net.UnknownHostException) {
            if (cached != null) {
                Result.success(cached.response)
            } else {
                Result.failure(IOException("Weather information is currently unavailable."))
            }
        } catch (e: IOException) {
            if (cached != null) {
                Result.success(cached.response)
            } else {
                Result.failure(IOException("Weather information is currently unavailable."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        locationName: String = "Current Location",
        forceRefresh: Boolean = false
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        val fetchResult = fetchWeather(lat, lon, forceRefresh)
        fetchResult.fold(
            onSuccess = { response ->
                val current = response.current
                if (current == null) {
                    return@withContext Result.failure(Exception("Unable to retrieve weather information."))
                }

                val temp = current.temperature2m ?: 20.0
                val feelsLike = current.apparentTemperature ?: temp
                val rainProb = current.precipitationProbability ?: 0
                val humidity = current.relativeHumidity2m ?: 50
                val wind = current.windSpeed10m ?: 0.0
                val code = current.weatherCode ?: 0
                val condition = WeatherCondition.fromWmoCode(code)
                val recommendation = WeatherRecommendationEngine.evaluateRecommendation(
                    temperatureC = temp,
                    rainProbability = rainProb,
                    windSpeedKmh = wind,
                    condition = condition,
                    taskType = TaskType.FLEXIBLE
                )

                val weatherData = WeatherData(
                    timestampMillis = System.currentTimeMillis(),
                    temperatureC = temp,
                    feelsLikeC = feelsLike,
                    rainProbability = rainProb,
                    humidityPercent = humidity,
                    windSpeedKmh = wind,
                    condition = condition,
                    recommendation = recommendation,
                    locationName = locationName
                )
                Result.success(weatherData)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun getForecastForTime(
        lat: Double,
        lon: Double,
        locationName: String = "Current Location",
        targetTimeMillis: Long,
        taskType: TaskType = TaskType.FLEXIBLE
    ): Result<WeatherData?> = withContext(Dispatchers.IO) {
        val fetchResult = fetchWeather(lat, lon)
        fetchResult.fold(
            onSuccess = { response ->
                val hourly = response.hourly
                if (hourly == null || hourly.time.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("Forecast is not available for this date yet."))
                }

                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT)
                // Set timezone if available from response, or use device timezone
                if (!response.timezone.isNullOrBlank()) {
                    try {
                        isoFormat.timeZone = TimeZone.getTimeZone(response.timezone)
                    } catch (_: Exception) {}
                }

                var closestIndex = -1
                var minDiff = Long.MAX_VALUE

                val times = hourly.time
                val count = times.size

                for (i in 0 until count) {
                    val timeStr = times[i]
                    val parsedDate = try {
                        isoFormat.parse(timeStr)
                    } catch (_: Exception) {
                        null
                    } ?: continue

                    val diff = Math.abs(parsedDate.time - targetTimeMillis)
                    if (diff < minDiff) {
                        minDiff = diff
                        closestIndex = i
                    }
                }

                // If the target time is further than 18 hours from the closest forecast point,
                // it means it is out of the 16-day forecast horizon or in the past
                val maxAllowedDiffMs = 18 * 60 * 60 * 1000L
                if (closestIndex == -1 || minDiff > maxAllowedDiffMs) {
                    // Out of range
                    return@withContext Result.success(null)
                }

                val temp = hourly.temperature2m?.getOrNull(closestIndex) ?: 20.0
                val feelsLike = hourly.apparentTemperature?.getOrNull(closestIndex) ?: temp
                val rainProb = hourly.precipitationProbability?.getOrNull(closestIndex) ?: 0
                val humidity = hourly.relativeHumidity2m?.getOrNull(closestIndex) ?: 50
                val wind = hourly.windSpeed10m?.getOrNull(closestIndex) ?: 0.0
                val code = hourly.weatherCode?.getOrNull(closestIndex) ?: 0
                val condition = WeatherCondition.fromWmoCode(code)

                val recommendation = WeatherRecommendationEngine.evaluateRecommendation(
                    temperatureC = temp,
                    rainProbability = rainProb,
                    windSpeedKmh = wind,
                    condition = condition,
                    taskType = taskType
                )

                val weatherData = WeatherData(
                    timestampMillis = targetTimeMillis,
                    temperatureC = temp,
                    feelsLikeC = feelsLike,
                    rainProbability = rainProb,
                    humidityPercent = humidity,
                    windSpeedKmh = wind,
                    condition = condition,
                    recommendation = recommendation,
                    locationName = locationName
                )
                Result.success(weatherData)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun getTodayHourlyForecast(
        lat: Double,
        lon: Double,
        locationName: String = "Current Location"
    ): Result<List<WeatherData>> = withContext(Dispatchers.IO) {
        val fetchResult = fetchWeather(lat, lon)
        fetchResult.fold(
            onSuccess = { response ->
                val hourly = response.hourly ?: return@withContext Result.success(emptyList())
                val times = hourly.time ?: return@withContext Result.success(emptyList())

                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT)
                if (!response.timezone.isNullOrBlank()) {
                    try {
                        isoFormat.timeZone = TimeZone.getTimeZone(response.timezone)
                    } catch (_: Exception) {}
                }

                val now = System.currentTimeMillis()
                val next24h = now + 24 * 60 * 60 * 1000L
                val resultList = mutableListOf<WeatherData>()

                for (i in times.indices) {
                    val timeStr = times[i]
                    val parsedDate = try {
                        isoFormat.parse(timeStr)
                    } catch (_: Exception) {
                        null
                    } ?: continue

                    val itemTime = parsedDate.time
                    if (itemTime >= now - 60 * 60 * 1000L && itemTime <= next24h) {
                        val temp = hourly.temperature2m?.getOrNull(i) ?: 20.0
                        val feelsLike = hourly.apparentTemperature?.getOrNull(i) ?: temp
                        val rainProb = hourly.precipitationProbability?.getOrNull(i) ?: 0
                        val humidity = hourly.relativeHumidity2m?.getOrNull(i) ?: 50
                        val wind = hourly.windSpeed10m?.getOrNull(i) ?: 0.0
                        val code = hourly.weatherCode?.getOrNull(i) ?: 0
                        val condition = WeatherCondition.fromWmoCode(code)
                        val recommendation = WeatherRecommendationEngine.evaluateRecommendation(
                            temperatureC = temp,
                            rainProbability = rainProb,
                            windSpeedKmh = wind,
                            condition = condition
                        )

                        resultList.add(
                            WeatherData(
                                timestampMillis = itemTime,
                                temperatureC = temp,
                                feelsLikeC = feelsLike,
                                rainProbability = rainProb,
                                humidityPercent = humidity,
                                windSpeedKmh = wind,
                                condition = condition,
                                recommendation = recommendation,
                                locationName = locationName
                            )
                        )
                    }
                }
                Result.success(resultList)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun searchLocations(query: String): Result<List<LocationData>> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext Result.success(emptyList())
        try {
            val response = apiService.searchLocations(
                url = "${WeatherApiConfig.GEOCODING_BASE_URL}v1/search",
                name = query.trim()
            )
            val list = response.results?.map { dto ->
                val display = buildString {
                    append(dto.name)
                    if (!dto.admin1.isNullOrBlank()) append(", ${dto.admin1}")
                    if (!dto.country.isNullOrBlank()) append(", ${dto.country}")
                }
                LocationData(
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    locationName = display,
                    isManual = true
                )
            } ?: emptyList()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
