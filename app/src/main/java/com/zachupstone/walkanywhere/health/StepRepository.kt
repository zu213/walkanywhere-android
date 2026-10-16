package com.zachupstone.walkanywhere.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.zachupstone.walkanywhere.data.AppDatabase
import com.zachupstone.walkanywhere.data.StepsEntity
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import androidx.core.content.edit
import timber.log.Timber

class StepRepository(private val context: Context) {

    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun stepsSinceLastChecked() {
        val status = HealthConnectClient.getSdkStatus(context)
        Timber.d("StepRepo  status = $status") // want 3
        if (!hasPermissions()) return
        if (!isAvailable) return

        val tripDao = AppDatabase.getInstance(context).tripDao()
        val mainRouteId = tripDao.getSelectedRoute().first()?.route?.routeId ?: return
        val lastCheckedDateTime = getLastSynced()
        setLastSynced(LocalDateTime.now())
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        if (lastCheckedDateTime == null || lastCheckedDateTime.truncatedTo(ChronoUnit.MINUTES) == now) {
            return
        }

        val response = client.aggregateGroupByDuration(
            AggregateGroupByDurationRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    lastCheckedDateTime.truncatedTo(ChronoUnit.MINUTES),
                    now
                ),
                timeRangeSlicer = Duration.ofMinutes(1)
            )
        )

        val processedResponse = mutableListOf<StepsEntity>()
        var lastDate: LocalDate? = null
        for(datapoint in response) {
            val date = datapoint.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            if(date == lastDate) {
                processedResponse.last().steps += datapoint.result[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0
            } else {
                processedResponse.add(StepsEntity(
                    parentRouteId = mainRouteId,
                    date = date,
                    steps = datapoint.result[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0
                ))
                lastDate = date
            }
        }

        tripDao.insertSteps(processedResponse)
    }

    private val prefs = context.getSharedPreferences("steps", Context.MODE_PRIVATE)

    fun getLastSynced(): LocalDateTime? =
        prefs.getString("last_synced", null)?.let(LocalDateTime::parse)

    fun setLastSynced(date: LocalDateTime) {
        prefs.edit { putString("last_synced", date.toString()) }
    }

    val stepPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )

    suspend fun hasPermissions(): Boolean =
        isAvailable && client.permissionController.getGrantedPermissions().containsAll(stepPermissions)
}