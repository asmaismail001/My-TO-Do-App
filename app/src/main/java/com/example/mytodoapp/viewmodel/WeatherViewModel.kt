package com.example.mytodoapp.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mytodoapp.model.*
import com.example.mytodoapp.repository.WeatherRepository
import com.example.mytodoapp.util.LocationHelper
import com.example.mytodoapp.util.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OutdoorTasksSummary(
    val outdoorTaskCount: Int,
    val hasBadWeatherAlert: Boolean,
    val summaryText: String
)

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val appContext: Context
) : ViewModel() {

    private val prefsManager = PreferencesManager(appContext)

    var currentLocation by mutableStateOf<LocationData>(
        LocationData(
            latitude = prefsManager.getSavedLatitude(),
            longitude = prefsManager.getSavedLongitude(),
            locationName = prefsManager.getSavedLocationName(),
            isManual = prefsManager.isManualLocation()
        )
    )
        private set

    var currentWeatherState by mutableStateOf<WeatherUiState>(WeatherUiState.Idle)
        private set

    var taskWeatherState by mutableStateOf<WeatherUiState>(WeatherUiState.Idle)
        private set

    var todayHourlyForecast by mutableStateOf<List<WeatherData>>(emptyList())
        private set

    var isSearchingLocations by mutableStateOf(false)
        private set

    var locationSearchResults by mutableStateOf<List<LocationData>>(emptyList())
        private set

    private var searchJob: Job? = null

    init {
        loadCurrentWeather(appContext)
    }

    fun loadCurrentWeather(context: Context, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // If manual location is NOT set, attempt to get real device location
            if (!currentLocation.isManual) {
                if (LocationHelper.hasLocationPermission(context)) {
                    val freshLoc = LocationHelper.getCurrentLocation(context)
                    if (freshLoc != null) {
                        currentLocation = freshLoc
                        prefsManager.saveWeatherLocation(freshLoc.latitude, freshLoc.longitude, freshLoc.locationName, false)
                    }
                } else {
                    // Location permission required notification state if never set
                    if (currentWeatherState is WeatherUiState.Idle) {
                        currentWeatherState = WeatherUiState.LocationPermissionRequired
                    }
                }
            }

            currentWeatherState = WeatherUiState.Loading
            val result = weatherRepository.getCurrentWeather(
                lat = currentLocation.latitude,
                lon = currentLocation.longitude,
                locationName = currentLocation.locationName,
                forceRefresh = forceRefresh
            )

            result.fold(
                onSuccess = { data ->
                    currentWeatherState = WeatherUiState.Success(data)
                },
                onFailure = { error ->
                    currentWeatherState = WeatherUiState.Error(
                        error.message ?: "Unable to retrieve weather information."
                    )
                }
            )

            // Also load 24h hourly forecast for detailed sheet
            val hourlyResult = weatherRepository.getTodayHourlyForecast(
                lat = currentLocation.latitude,
                lon = currentLocation.longitude,
                locationName = currentLocation.locationName
            )
            hourlyResult.onSuccess { list ->
                todayHourlyForecast = list
            }
        }
    }

    fun loadTaskWeather(dueTimeMillis: Long?, taskType: TaskType = TaskType.FLEXIBLE) {
        if (dueTimeMillis == null) {
            taskWeatherState = WeatherUiState.Idle
            return
        }

        viewModelScope.launch {
            taskWeatherState = WeatherUiState.Loading
            val result = weatherRepository.getForecastForTime(
                lat = currentLocation.latitude,
                lon = currentLocation.longitude,
                locationName = currentLocation.locationName,
                targetTimeMillis = dueTimeMillis,
                taskType = taskType
            )

            result.fold(
                onSuccess = { data ->
                    if (data != null) {
                        taskWeatherState = WeatherUiState.Success(data)
                    } else {
                        taskWeatherState = WeatherUiState.ForecastUnavailable
                    }
                },
                onFailure = { error ->
                    taskWeatherState = WeatherUiState.Error(
                        error.message ?: "Unable to retrieve weather information."
                    )
                }
            )
        }
    }

    fun clearTaskWeather() {
        taskWeatherState = WeatherUiState.Idle
    }

    fun setManualLocation(lat: Double, lon: Double, name: String) {
        currentLocation = LocationData(
            latitude = lat,
            longitude = lon,
            locationName = name,
            isManual = true
        )
        prefsManager.saveWeatherLocation(lat, lon, name, true)
        loadCurrentWeather(appContext, forceRefresh = true)
    }

    fun useDeviceLocation(context: Context) {
        viewModelScope.launch {
            if (LocationHelper.hasLocationPermission(context)) {
                val loc = LocationHelper.getCurrentLocation(context)
                if (loc != null) {
                    currentLocation = loc
                    prefsManager.saveWeatherLocation(loc.latitude, loc.longitude, loc.locationName, false)
                } else {
                    currentLocation = currentLocation.copy(isManual = false)
                    prefsManager.saveWeatherLocation(currentLocation.latitude, currentLocation.longitude, "Current Location", false)
                }
                loadCurrentWeather(context, forceRefresh = true)
            } else {
                currentWeatherState = WeatherUiState.LocationPermissionRequired
            }
        }
    }

    fun searchCities(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            locationSearchResults = emptyList()
            isSearchingLocations = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce typing
            isSearchingLocations = true
            val res = weatherRepository.searchLocations(query)
            res.fold(
                onSuccess = { list ->
                    locationSearchResults = list
                    isSearchingLocations = false
                },
                onFailure = {
                    locationSearchResults = emptyList()
                    isSearchingLocations = false
                }
            )
        }
    }

    fun clearLocationSearchResults() {
        locationSearchResults = emptyList()
    }

    fun getOutdoorSummary(todayTasks: List<Todo>): OutdoorTasksSummary {
        val outdoorTasks = todayTasks.filter { !it.completed && (it.taskType == TaskType.OUTDOOR || it.taskType == TaskType.FLEXIBLE) }
        val count = outdoorTasks.size

        if (count == 0) {
            return OutdoorTasksSummary(
                outdoorTaskCount = 0,
                hasBadWeatherAlert = false,
                summaryText = "No outdoor tasks scheduled for today"
            )
        }

        val currentWeather = (currentWeatherState as? WeatherUiState.Success)?.data
        val isRainy = currentWeather?.condition?.iconType in listOf(
            WeatherIconType.RAIN, WeatherIconType.HEAVY_RAIN, WeatherIconType.THUNDERSTORM
        ) || (currentWeather?.rainProbability ?: 0) >= 50

        return if (isRainy) {
            OutdoorTasksSummary(
                outdoorTaskCount = count,
                hasBadWeatherAlert = true,
                summaryText = "$count outdoor task${if (count > 1) "s" else ""} • Rain likely today"
            )
        } else {
            OutdoorTasksSummary(
                outdoorTaskCount = count,
                hasBadWeatherAlert = false,
                summaryText = "$count outdoor task${if (count > 1) "s" else ""} • Good conditions"
            )
        }
    }
}

class WeatherViewModelFactory(
    private val weatherRepository: WeatherRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WeatherViewModel(weatherRepository, appContext) as T
    }
}
