package com.zachupstone.walkanywhere.ui.routes

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.data.db.AppDatabase
import com.zachupstone.walkanywhere.data.db.RouteWithSteps
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RouteWithPercent (
    val routeWithSteps: RouteWithSteps,
    val percent: Float
)

class RoutesViewModel: ViewModel() {
    private val _routes = mutableStateOf<List<RouteWithPercent>?>(null)
    val routes: State<List<RouteWithPercent>?> = _routes

    private val _loading = mutableStateOf<Boolean>(true)
    val loading: State<Boolean> = _loading

    fun fetchAllRoutes(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            val routesWithSteps = tripDao.getAllRoutes().first()
            _routes.value = routesWithSteps.map { routeWithSteps ->
                val lastResetPos = routeWithSteps.steps.indexOfLast {
                    it.steps == -1
                }

                val stepsToCheck = if(lastResetPos > 0) routeWithSteps.steps.subList(lastResetPos, routeWithSteps.steps.count()) else routeWithSteps.steps

                RouteWithPercent(routeWithSteps, ((stepsToCheck.sumOf { it.steps }).toFloat() / routeWithSteps.route.totalSteps).coerceAtMost(1f).coerceAtLeast(0f))
            }
            _loading.value = false
        }
    }

    fun deleteRoute(context: Context, routeId: Int) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            tripDao.deleteRoute(routeId)
            fetchAllRoutes(context)
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