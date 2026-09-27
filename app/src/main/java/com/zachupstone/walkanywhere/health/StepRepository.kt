package com.zachupstone.walkanywhere.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.StepsEntity
import kotlinx.coroutines.flow.first
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

class StepRepository(private val context: Context) {

    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun stepsSinceLastChecked() {
        val status = HealthConnectClient.getSdkStatus(context)
        Log.d("StepRepo", "status = $status") // want 3

        if (!isAvailable) return

        val tripDao = AppDatabase.getInstance(context).tripDao()
        val mainRouteId = tripDao.getSelectedRoute().first()?.route?.routeId ?: return
        val lastCheckedDate = getLastSynced()
        setLastSynced(LocalDate.now())
        if (lastCheckedDate == null) {
            return
        }

        val response = client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    lastCheckedDate.atStartOfDay(),
                    LocalDateTime.now()
                ),
                timeRangeSlicer = Period.ofDays(1)
            )
        )

        Log.d("Step Data", "Step count today since last laucnh: ${response.first()}")
        val days = response.map { bucket ->
            val date = bucket.startTime.toLocalDate()
            StepsEntity(
                parentRouteId = mainRouteId,
                date = Date.valueOf(date.toString()),
                steps = bucket.result[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0
            )
        }

        tripDao.insertSteps(days)
    }

    private val prefs = context.getSharedPreferences("steps", Context.MODE_PRIVATE)

    fun getLastSynced(): LocalDate? =
        prefs.getString("last_synced", null)?.let(LocalDate::parse)

    fun setLastSynced(date: LocalDate) {
        prefs.edit().putString("last_synced", date.toString()).apply()
    }
}