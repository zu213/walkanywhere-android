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
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.RouteWithSteps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class StepsViewModel: ViewModel() {

    private val _routes = mutableStateOf<List<RouteWithSteps>?>(null)
    val routes: State<List<RouteWithSteps>?> = _routes

    fun fetchAllRoutes(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            _routes.value = tripDao.getAllRoutes().first()
        }
    }
}