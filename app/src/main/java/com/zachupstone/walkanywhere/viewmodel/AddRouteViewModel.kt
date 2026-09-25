package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.BuildConfig
import com.zachupstone.walkanywhere.api.DirectionsDto
import com.zachupstone.walkanywhere.api.RetrofitClient
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.RouteEntity
import kotlinx.coroutines.launch

class AddRouteViewModel: ViewModel() {
    private val _routePolyline = mutableStateOf<List<LatLng>?>(null)
    val routePolyline: State<List<LatLng>?> = _routePolyline
    private var origin: LatLng? = null
    private var destination: LatLng? = null

    fun addRoute(context: Context, requestOrigin: LatLng, requestDestination: LatLng) {
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
            tripDao.insertRoute(
                RouteEntity(
                    origin = origin!!,
                    destination = destination!!,
                    encodedPolyline = _routePolyline.value.toString()
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
}