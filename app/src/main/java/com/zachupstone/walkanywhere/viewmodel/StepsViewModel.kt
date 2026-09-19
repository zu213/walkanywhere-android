package com.zachupstone.walkanywhere.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.BuildConfig
import com.zachupstone.walkanywhere.api.DirectionsDto
import com.zachupstone.walkanywhere.api.RetrofitClient
import kotlinx.coroutines.launch
import timber.log.Timber

class StepsViewModel: ViewModel() {

    // State to hold the user's location as LatLng (latitude and longitude)
    // The UI will watch this state for updates
    private val _directionsResult = mutableStateOf<DirectionsDto?>(null)
    private val _routePolyline = mutableStateOf<List<LatLng>?>(null)
    val directionsResult: State<DirectionsDto?> = _directionsResult
    val routePolyline: State<List<LatLng>?> = _routePolyline

    fun fetchDirections(origin: LatLng, destination: LatLng) {
        // viewModelScope ensures the network call cancels safely if the user closes the screen
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mapDirectionsService.getDirections(
                    "${origin.latitude},${origin.longitude}",
                    "${destination.latitude},${destination.longitude}",
                    BuildConfig.MAPS_API_KEY,
                )
                if (response.isSuccessful) {
                    _directionsResult.value = response.body()
                    _directionsResult.value?.routes?.first()?.overview_polyline?.points?.let {
                        _routePolyline.value = DirectionsDto.Route.Leg.Step.Polyline.decodePolyline(it)
                    }
                }
            } catch (e: Exception) {
                // Handle your error here (e.g., no internet connection)
            }
        }
    }

    fun clearDirections() {
        _directionsResult.value = null
        _routePolyline.value = null
    }
}