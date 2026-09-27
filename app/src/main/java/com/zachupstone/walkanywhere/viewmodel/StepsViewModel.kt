package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.StepsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.time.Instant

data class DayWithSteps(
    val date: Date,
    val steps: MutableList<StepsEntity>
)

class StepsViewModel: ViewModel() {

    private var _steps: List<StepsEntity>? = null
    private var _oldestDate = Date.from(Instant.now())
    private val _days = mutableStateOf<List<DayWithSteps>?>(null)
    val days: MutableState<List<DayWithSteps>?> = _days

    fun fetchAllSteps(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            _steps = tripDao.getAllSteps().first()
            _steps?.let { steps ->
                if(steps.size < 1) return@launch
                steps.last().date.let {
                    _oldestDate = it
                }
                var lastDate: Date? = null
                val tempDays: MutableList<DayWithSteps> = mutableListOf()
                var lastDateRoutes = mutableListOf<Int>()
                for(step in steps) {
                    if(lastDate == step.date){
                        // Cover opening app in day case
                        if(lastDateRoutes.contains(step.routeId)) {
                            tempDays.last().steps.first { it.routeId == step.routeId }.steps += step.steps
                        } else {
                            lastDateRoutes.add(step.routeId)
                            tempDays.last().steps.add(step)
                        }
                    } else {
                        lastDateRoutes = mutableListOf()
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