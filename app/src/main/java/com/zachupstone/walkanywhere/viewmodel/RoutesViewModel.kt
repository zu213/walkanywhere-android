package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.RouteWithSteps
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RouteWithPercent (
    val routeWithSteps: RouteWithSteps,
    val percent: Float
)

class RoutesViewModel: ViewModel() {
    private val _routes = mutableStateOf<List<RouteWithPercent>?>(null)
    val routes: State<List<RouteWithPercent>?> = _routes

    fun fetchAllRoutes(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            val routesWithSteps = tripDao.getAllRoutes().first()
            _routes.value = routesWithSteps.map { routeWithSteps ->
                RouteWithPercent(routeWithSteps, (routeWithSteps.steps.sumOf { it.steps }).toFloat() / routeWithSteps.route.totalSteps)
            }
        }
    }

    fun deleteRoute(context: Context, routeId: Int) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            tripDao.deleteRoute(routeId)
        }
    }

    fun favouriteRoute(context: Context, routeId: Int) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            tripDao.selectRoute(routeId)
            fetchAllRoutes(context)
        }
    }
}