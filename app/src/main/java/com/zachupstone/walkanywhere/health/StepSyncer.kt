package com.zachupstone.walkanywhere.health

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.fitness.FitnessLocal.getLocalRecordingClient
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.data.LocalField
import com.google.android.gms.fitness.request.LocalDataReadRequest
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class StepSyncer(private val context: Context) {

    fun hasPermissions(): Boolean {
        return (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context,
            LocalRecordingClient.LOCAL_RECORDING_CLIENT_MIN_VERSION_CODE
        ) == ConnectionResult.SUCCESS)
                && (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    fun subscribeToFitnessService() {

        // Check if they are able to do activity recording of steps then setup worker to record said steps
        // Early return if permission denied
        if(!hasPermissions()) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        getLocalRecordingClient(context)
            .subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.d("Steps", "subscribed")
                scheduleStepSync()
            }
            .addOnFailureListener { Log.e("Steps", "subscribe failed", it) }
    }

    private fun scheduleStepSync() {
        val work = PeriodicWorkRequestBuilder<StepSyncerWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("step-sync", ExistingPeriodicWorkPolicy.KEEP, work)
    }
}

class StepSyncerWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hc = HealthConnectClient.getOrCreate(applicationContext)
        val writePerm = HealthPermission.getWritePermission(StepsRecord::class)
        if (writePerm !in hc.permissionController.getGrantedPermissions()) return Result.failure()

        // last 2 days of complete hours, re-synced every run
        val end = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS)
        val start = end.minusDays(2)

        val request = LocalDataReadRequest.Builder()
            .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.HOURS)
            .setTimeRange(start.toEpochSecond(), end.toEpochSecond(), TimeUnit.SECONDS)
            .build()

        val response = try {
            getLocalRecordingClient(applicationContext)
                .readData(request).await()
        } catch (e: Exception) {
            return Result.retry()
        }

        val records = response.buckets.mapNotNull { bucket ->
            val steps = bucket.dataSets.flatMap { it.dataPoints }
                .sumOf { it.getValue(LocalField.FIELD_STEPS).asInt() }
            if (steps == 0) return@mapNotNull null

            val s = Instant.ofEpochSecond(bucket.getStartTime(TimeUnit.SECONDS))
            val e = Instant.ofEpochSecond(bucket.getEndTime(TimeUnit.SECONDS))

            StepsRecord(
                startTime = s, startZoneOffset = null,
                endTime = e, endZoneOffset = null,
                count = steps.toLong(),
                metadata = Metadata.autoRecorded(
                    device = Device(type = Device.TYPE_PHONE),
                    clientRecordId = "walkanywhere-steps-${s.epochSecond}",
                    clientRecordVersion = steps.toLong()
                )
            )
        }

        if (records.isNotEmpty()) hc.insertRecords(records)
        return Result.success()
    }
}