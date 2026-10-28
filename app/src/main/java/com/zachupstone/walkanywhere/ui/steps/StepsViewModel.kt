package com.zachupstone.walkanywhere.ui.steps

import android.app.Application
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.data.db.AppDatabase
import com.zachupstone.walkanywhere.data.db.StepsEntity
import kotlinx.coroutines.flow.first
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DayWithSteps(
    val date: LocalDate,
    val steps: MutableList<StepsEntity>,
    var totalSteps: Int
)

class StepsViewModel(application: Application): AndroidViewModel(application) {

    private val tripDao = AppDatabase.getInstance(application).tripDao()
    private var _steps: List<StepsEntity>? = null
    private var _oldestDate = LocalDate.now()
    private val _days = mutableStateOf<List<DayWithSteps>?>(null)
    val days: MutableState<List<DayWithSteps>?> = _days
    var routeColourMap: StateFlow<Map<Int, Color>> =  tripDao.getAllRoutes().map { routes ->
        routes.associate { it.route.routeId to Color(it.route.colour).copy(alpha = 1f) }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _loading = mutableStateOf(true)
    val loading = _loading

    fun fetchAllSteps(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()

            _steps = tripDao.getAllSteps().first()
            _steps?.let { steps ->
                if(steps.isEmpty()) {
                    _days.value = emptyList()
                    _loading.value = false
                    return@launch
                }

                steps.last().date.let { _oldestDate = it }
                var lastDate: LocalDate? = null
                val tempDays: MutableList<DayWithSteps> = mutableListOf()
                var lastDateRoutes = mutableListOf<Int>()

                for(step in steps) {
                    if(step.steps == -1) {
                        continue
                    }
                    if(lastDate == step.date){
                        // Cover opening app in day case
                        if(lastDateRoutes.contains(step.parentRouteId)) {
                            tempDays.last().steps.first { it.parentRouteId == step.parentRouteId }.steps += step.steps
                        } else {
                            lastDateRoutes.add(step.parentRouteId)
                            tempDays.last().steps.add(step)
                        }
                        tempDays.last().totalSteps += step.steps
                    } else {
                        if(tempDays.isNotEmpty()) {
                            var date = tempDays.last().date.plusDays(1)
                            while (date.isBefore(step.date)) {
                                tempDays.add(DayWithSteps(date, mutableListOf(), 0))
                                date = date.plusDays(1)
                            }
                        }

                        lastDateRoutes = mutableListOf(step.parentRouteId)
                        lastDate = step.date
                        tempDays.add(DayWithSteps(step.date, mutableListOf(step), step.steps))
                    }
                }

                var date = tempDays.last().date.plusDays(1)
                while(date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now())) {
                    tempDays.add(DayWithSteps(date, mutableListOf(), 0))
                    date = date.plusDays(1)
                }

                _days.value = tempDays.reversed()
            }
            _loading.value = false
        }
    }
}