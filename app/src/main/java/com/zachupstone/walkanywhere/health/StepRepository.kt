package com.zachupstone.walkanywhere.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.zachupstone.walkanywhere.data.AppDatabase
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId

class StepRepository(private val context: Context) {

    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun stepsSinceLastChecked(): List<Pair<LocalDate, Long>>? {
        if (!isAvailable) return null

        val tripDao = AppDatabase.getInstance(context).tripDao()
        val lastCheckedDate = tripDao.lastSyncedDate() ?: return null

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
            bucket.startTime.toLocalDate() to (bucket.result[StepsRecord.COUNT_TOTAL] ?: 0L)
        }

        return days
    }
}