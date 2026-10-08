package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLngBounds
import com.zachupstone.walkanywhere.api.DirectionsDto
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.RouteEntity
import com.zachupstone.walkanywhere.data.RouteWithSteps
import com.zachupstone.walkanywhere.util.decodeDBPolylineString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

// Pretty arbitrary atm
const val stepDistance = 0.00001

class MapViewModel: ViewModel() {
    private val _routePolyline = mutableStateOf<List<LatLng>?>(null)
    val routePolyline: State<List<LatLng>?> = _routePolyline
    private val _origin = mutableStateOf<LatLng?>(null)
    val origin: State<LatLng?> = _origin
    private val _destination = mutableStateOf<LatLng?>(null)
    val destination: State<LatLng?> = _destination
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation = _userLocation
    private val _startingCameraLocation = mutableStateOf<LatLngBounds?>(null)
    val startingCameraLocation = _startingCameraLocation

    fun setRoute(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            val route = tripDao.getSelectedRoute().first()
            route?.let {
                setRoute(it)
            }
        }
    }

    fun setRoute(route: RouteWithSteps) {
        // viewModelScope ensures the network call cancels safely if the user closes the screen
        viewModelScope.launch {
            route.route?.let {
                _origin.value = it.origin
                _destination.value = it.destination
                // need to fix how encoded probably
                _routePolyline.value = decodeDBPolylineString(it.encodedPolyline)
                route.steps
            }
            findPosition(route)
            findStartingCameraLocation()
        }

    }

    fun findPosition(route: RouteWithSteps) {
        val totalSteps = route.steps.sumOf { it.steps }
        val pol = routePolyline.value ?: return

        var routeComplete = false
        var percentageWalked = 0.0

        var stepCounter = 0
        var polPosition = 0
        while(true) {
            if (polPosition + 1 >= pol.count()) {
                routeComplete = true
                break
            }
            val nextSteps = getStepsBetwixt(pol[polPosition], pol[polPosition + 1])
            if (stepCounter + nextSteps > totalSteps) {
                percentageWalked = (totalSteps - stepCounter).toDouble() / nextSteps
                break
            }
            stepCounter += nextSteps
            polPosition++
        }

        if(routeComplete) {
            _userLocation.value = _destination.value
        }

        val startPoint = pol[polPosition]
        val endPoint = pol[polPosition + 1]
        val vector = LatLng(
            (endPoint.latitude - startPoint.latitude) * percentageWalked,
            (endPoint.longitude - startPoint.longitude) * percentageWalked
        )

        _userLocation.value = LatLng(startPoint.latitude + vector.latitude, startPoint.longitude + vector.longitude)
    }

    fun getStepsBetwixt(start: LatLng, finish: LatLng): Int {
        // Good old pythag
        val distance = sqrt((start.latitude - finish.latitude).pow(2) + (start.longitude - finish.longitude).pow(2))
        if(distance == 0.0) return 1;
        return (distance / stepDistance).roundToInt()
    }

    fun findStartingCameraLocation() {
        val pol = routePolyline.value ?: return
        if(pol.isEmpty()) return
        _startingCameraLocation.value = LatLngBounds.builder().apply {
            pol.forEach { include(it) }
        }.build()

    }
}