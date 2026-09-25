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