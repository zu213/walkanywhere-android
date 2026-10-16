package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.BuildConfig
import com.zachupstone.walkanywhere.api.DirectionsDto
import com.zachupstone.walkanywhere.api.RetrofitClient
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.RouteEntity
import com.zachupstone.walkanywhere.ui.theme.PillColours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

class AddRouteViewModel: ViewModel() {
    private val _routePolyline = mutableStateOf<List<LatLng>?>(null)
    val routePolyline: State<List<LatLng>?> = _routePolyline
    private var origin: LatLng? = null
    private var destination: LatLng? = null
    private val _cameraLocation = mutableStateOf<LatLng?>(null)
    val cameraLocation = _cameraLocation

    fun addRoute(requestOrigin: LatLng, requestDestination: LatLng) {
        // viewModelScope ensures the network call cancels safely if the user closes the screen
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mapDirectionsService.getDirections(
                    "${requestOrigin.latitude},${requestOrigin.longitude}",
                    "${requestDestination.latitude},${requestDestination.longitude}",
                    BuildConfig.MAPS_API_KEY,
                )
                if (response.isSuccessful) {
                    val directionsResult = response.body()
                    directionsResult?.routes?.first()?.legs?.first()?.steps
                    directionsResult?.routes?.first()?.overview_polyline?.points?.let {
                        val polyline = DirectionsDto.Route.Leg.Step.Polyline.decodePolyline(it)
                        _routePolyline.value = polyline
                        origin = requestOrigin
                        destination = requestDestination
                        return@launch
                    }
                    _directionsError.value = "No routes found :("
                }
            } catch (e: Exception) {
                _directionsError.value = e.localizedMessage
                // Handle your error here (e.g., no internet connection)
            }
        }
    }

    fun saveRoute(context: Context) {
        if(origin == null || destination == null || _routePolyline.value == null) {
            return
        }
        viewModelScope.launch {

            val tripDao = AppDatabase.getInstance(context).tripDao()

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
    fun selectLocation(selectedPlace: String, context: Context) {
        viewModelScope.launch {
            val geocoder = Geocoder(context)
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