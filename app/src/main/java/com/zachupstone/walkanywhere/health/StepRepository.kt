package com.zachupstone.walkanywhere.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.StepsEntity
import kotlinx.coroutines.flow.first
import java.sql.Date
import java.time.LocalDateTime
import java.time.Period

class StepRepository(private val context: Context) {

    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun stepsSinceLastChecked() {
        if (!isAvailable) return

        val tripDao = AppDatabase.getInstance(context).tripDao()
        val mainRouteId = tripDao.getSelectedRoute().first()?.route?.routeId ?: return
        val lastCheckedDate = tripDao.lastSyncedDate() ?: return

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
}