package com.example.mytodoapp.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.mytodoapp.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LocationData? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        // 1. Try to get the freshest last known location
        var bestLocation: Location? = null
        val providers = locationManager.getProviders(true)
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }

        // If last known location is very recent (less than 15 minutes old), use it directly
        val fifteenMinutesAgo = System.currentTimeMillis() - 15 * 60 * 1000L
        if (bestLocation != null && bestLocation.time > fifteenMinutesAgo) {
            val name = resolveCityName(context, bestLocation.latitude, bestLocation.longitude)
            return@withContext LocationData(
                latitude = bestLocation.latitude,
                longitude = bestLocation.longitude,
                locationName = name,
                isManual = false
            )
        }

        // 2. Request a fresh location with timeout
        val freshLocation = withTimeoutOrNull(4000L) {
            suspendCancellableCoroutine<Location?> { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(loc)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                val preferredProvider = when {
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                    else -> null
                }

                if (preferredProvider != null) {
                    try {
                        locationManager.requestSingleUpdate(preferredProvider, listener, null)
                    } catch (e: Exception) {
                        if (continuation.isActive) continuation.resume(null)
                    }
                } else {
                    if (continuation.isActive) continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    try {
                        locationManager.removeUpdates(listener)
                    } catch (_: Exception) {}
                }
            }
        }

        val finalLoc = freshLocation ?: bestLocation
        if (finalLoc != null) {
            val name = resolveCityName(context, finalLoc.latitude, finalLoc.longitude)
            return@withContext LocationData(
                latitude = finalLoc.latitude,
                longitude = finalLoc.longitude,
                locationName = name,
                isManual = false
            )
        }

        return@withContext null
    }

    private fun resolveCityName(context: Context, latitude: Double, longitude: Double): String {
        return try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var result = "Current Location"
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val city = address.locality ?: address.subAdminArea ?: address.adminArea
                        if (!city.isNullOrBlank()) {
                            result = city
                        }
                    }
                    result
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        address.locality ?: address.subAdminArea ?: address.adminArea ?: "Current Location"
                    } else {
                        "Current Location"
                    }
                }
            } else {
                "Current Location"
            }
        } catch (e: Exception) {
            "Current Location"
        }
    }
}
