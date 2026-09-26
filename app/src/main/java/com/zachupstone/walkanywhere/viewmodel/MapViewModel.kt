package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.api.DirectionsDto
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.util.decodeDBPolylineString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MapViewModel: ViewModel() {
    private val _routePolyline = mutableStateOf<List<LatLng>?>(null)
    val routePolyline: State<List<LatLng>?> = _routePolyline
    private val _origin = mutableStateOf<LatLng?>(null)
    val origin: State<LatLng?> = _origin
    private val _destination = mutableStateOf<LatLng?>(null)
    val destination: State<LatLng?> = _destination

    fun fetchMainRoute(context: Context) {
        // viewModelScope ensures the network call cancels safely if the user closes the screen
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            val route = tripDao.getSelectedRoute().first()
            route?.route?.let {
                _origin.value = it.origin
                _destination.value = it.destination
                // need to fix how encoded probably
                _routePolyline.value = decodeDBPolylineString(it.encodedPolyline)
            }
        }
    }
}