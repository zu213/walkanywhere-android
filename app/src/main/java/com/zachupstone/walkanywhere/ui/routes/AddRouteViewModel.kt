package com.zachupstone.walkanywhere.ui.routes

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import android.location.Geocoder
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.BuildConfig
import com.zachupstone.walkanywhere.data.api.DirectionsDto
import com.zachupstone.walkanywhere.data.api.RetrofitClient
import com.zachupstone.walkanywhere.ui.map.stepDistance
import com.zachupstone.walkanywhere.data.db.AppDatabase
import com.zachupstone.walkanywhere.data.db.RouteEntity
import com.zachupstone.walkanywhere.ui.theme.PillColours
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

class AddRouteViewModel(application: Application): AndroidViewModel(application) {
    private val tripDao = AppDatabase.getInstance(application).tripDao()
    private val _routePolyline = mutableStateOf<List<LatLng>?>(null)
    val routePolyline: State<List<LatLng>?> = _routePolyline
    private var origin: LatLng? = null
    private var destination: LatLng? = null
    private val _cameraLocation = mutableStateOf<LatLng?>(null)
    val cameraLocation: State<LatLng?> = _cameraLocation

    fun addRoute(requestOrigin: LatLng, requestDestination: LatLng) {
        // viewModelScope ensures the network call cancels safely if the user closes the screen
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mapDirectionsService.getDirections(
                    "${requestOrigin.latitude},${requestOrigin.longitude}",
                    "${requestDestination.latitude},${requestDestination.longitude}",
                    BuildConfig.MAPS_API_KEY,
                    getApplication<Application>().packageName,
                    signingCertSha1(),
                )
                val directionsResult = response.body()
                Log.w("AddRoute", "cert=${signingCertSha1()} http=${response.code()} status=${directionsResult?.status} error=${directionsResult?.error_message}")
                if (response.isSuccessful) {
                    directionsResult?.routes?.firstOrNull()?.overview_polyline?.points?.let {
                        val polyline = DirectionsDto.Route.Leg.Step.Polyline.decodePolyline(it)
                        _routePolyline.value = polyline
                        origin = requestOrigin
                        destination = requestDestination
                        return@launch
                    }
                    _directionsError.value = directionsResult?.error_message
                        ?: "No routes found (${directionsResult?.status})"
                }
            } catch (e: Exception) {
                _directionsError.value = e.localizedMessage
                // Handle your error here (e.g., no internet connection)
            }
        }
    }

    // Android-restricted keys only work on web-service calls if the app identifies itself; read the
    // cert at runtime because Play App Signing re-signs the release build with a different key.
    private fun signingCertSha1(): String {
        val app = getApplication<Application>()
        val cert = app.packageManager
            .getPackageInfo(app.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            .signingInfo!!.apkContentsSigners.first().toByteArray()
        return MessageDigest.getInstance("SHA-1").digest(cert).joinToString("") { "%02X".format(it) }
    }

    fun saveRoute() {
        if(origin == null || destination == null || _routePolyline.value == null) {
            return
        }
        viewModelScope.launch {

            var totalSteps = 0
            _routePolyline.value?.let {
                for (i in 0..<it.count() - 1) {
                    totalSteps += (sqrt((it[i].latitude - it[i + 1].latitude).pow(2.0) + (it[i].longitude - it[i + 1].longitude).pow(2.0)) / stepDistance).toInt()
                }
            }

            tripDao.insertRoute(
                RouteEntity(
                    origin = origin!!,
                    destination = destination!!,
                    encodedPolyline = _routePolyline.value.toString(),
                    colour = PillColours.random().toArgb(),
                    totalSteps = totalSteps
                )
            )
        }
    }

    fun clearDirections() {
        _routePolyline.value = null
    }

    private val _directionsError = mutableStateOf<String?>(null)
    val directionsError: State<String?> = _directionsError

    fun clearDirectionsError() {
        _directionsError.value = null
    }

    // Function to geocode the selected place and update the selected location state
    fun selectLocation(selectedPlace: String) {
        viewModelScope.launch {
            val geocoder = Geocoder(getApplication())
            val addresses = withContext(Dispatchers.IO) {
                // Perform geocoding on a background thread
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(selectedPlace, 1)
            }
            if (!addresses.isNullOrEmpty()) {
                // Update the selected location in the state
                val address = addresses[0]
                _cameraLocation.value = LatLng(address.latitude, address.longitude)
            } else {
                Timber.tag("MapScreen").e("No location found for the selected place.")
            }
        }
    }
}