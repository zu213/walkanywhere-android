package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.RouteEntity
import com.zachupstone.walkanywhere.data.RouteWithSteps
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RoutesViewModel: ViewModel() {
    private val _routes = mutableStateOf<List<RouteWithSteps>?>(null)
    val routes: State<List<RouteWithSteps>?> = _routes

    fun fetchAllRoutes(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            _routes.value = tripDao.getAllRoutes().first()
        }
    }

    fun createRoute(context: Context, origin: LatLng, destination: LatLng, polyline: Polyline) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            tripDao.insertRoute(
                RouteEntity(
                    origin=origin,
                    destination = destination,
                    encodedPolyline = polyline.toString()
                )
            )
        }
    }

    fun deleteRoute(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            tripDao
        }
    }
}