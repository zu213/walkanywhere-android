package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.StepsEntity
import kotlinx.coroutines.flow.first
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import java.util.Date
import java.time.Instant
import kotlin.collections.mutableMapOf
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.flow.map

data class DayWithSteps(
    val date: Date,
    val steps: MutableList<StepsEntity>
)

class StepsViewModel: ViewModel() {

    private var _steps: List<StepsEntity>? = null
    private var _oldestDate = Date.from(Instant.now())
    private val _days = mutableStateOf<List<DayWithSteps>?>(null)
    val days: MutableState<List<DayWithSteps>?> = _days
    private val _routeColourMap = mutableStateMapOf<Int, Color>()
    val routeColourMap: Map<Int, Color> = _routeColourMap

    fun fetchAllSteps(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            val routes = tripDao.getAllRoutes()
            routes.map { routes ->
                routes.associate { it.route.routeId to Color(it.route.colour.toColorInt()) }
            }


            _steps = tripDao.getAllSteps().first()
            _steps?.let { steps ->
                if(steps.isEmpty()) return@launch

                steps.last().date.let { _oldestDate = it }
                var lastDate: Date? = null
                val tempDays: MutableList<DayWithSteps> = mutableListOf()
                var lastDateRoutes = mutableListOf<Int>()

                for(step in steps) {
                    if(lastDate == step.date){
                        // Cover opening app in day case
                        if(lastDateRoutes.contains(step.parentRouteId)) {
                            tempDays.last().steps.first { it.parentRouteId == step.parentRouteId }.steps += step.steps
                        } else {
                            lastDateRoutes.add(step.parentRouteId)
                            tempDays.last().steps.add(step)
                        }
                    } else {
                        lastDateRoutes = mutableListOf(step.parentRouteId)
                        lastDate = step.date
                        tempDays.add(DayWithSteps(step.date, mutableListOf(step)))
                    }
                }
                _days.value = tempDays
            }
            // rearrange steps into days
        }
    }
}