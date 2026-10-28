package com.zachupstone.walkanywhere.data.health

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zachupstone.walkanywhere.WalkAnywhereApplication
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.fitness.FitnessLocal.getLocalRecordingClient
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalBucket
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.data.LocalField
import com.google.android.gms.fitness.request.LocalDataReadRequest
import com.zachupstone.walkanywhere.MainActivity
import com.zachupstone.walkanywhere.R
import com.zachupstone.walkanywhere.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
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
                Timber.tag("Steps").d("subscribed")
                syncNow()
                scheduleStepSync()
            }
            .addOnFailureListener {
                // Timber.tag("Steps").d("subscribe failed")
            }
    }

    private fun syncNow() {
        val work = OneTimeWorkRequestBuilder<StepSyncerWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork("step-sync-now", ExistingWorkPolicy.KEEP, work)
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

        // last 2 days of minutes, re-synced every run
        val end = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1)
        val start = end.minusDays(2)

        // Read an hour at a time: Play services silently truncates large responses
        // ("Failed to deliver all batches to client"), dropping the most recent buckets.
        val buckets = mutableListOf<LocalBucket>()
        var chunkStart = start
        while (chunkStart.isBefore(end)) {
            val chunkEnd = chunkStart.plusHours(1)
            val request = LocalDataReadRequest.Builder()
                .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.MINUTES)
                .setTimeRange(chunkStart.toEpochSecond(), chunkEnd.toEpochSecond(), TimeUnit.SECONDS)
                .build()

            buckets += try {
                getLocalRecordingClient(applicationContext)
                    .readData(request).await().buckets
            } catch (_: Exception) {
                return Result.retry()
            }
            chunkStart = chunkEnd
        }

        val records = buckets.mapNotNull { bucket ->
            val steps = bucket.dataSets.flatMap { it.dataPoints }
                .sumOf { it.getValue(LocalField.FIELD_STEPS).asInt() }

            val s = Instant.ofEpochSecond(bucket.getStartTime(TimeUnit.SECONDS))
            val e = Instant.ofEpochSecond(bucket.getEndTime(TimeUnit.SECONDS))

            if (steps == 0) return@mapNotNull null

            StepsRecord(
                startTime = s, startZoneOffset = null,
                endTime = e, endZoneOffset = null,
                count = steps.toLong(),
                metadata = Metadata.autoRecorded(
                    device = Device(type = Device.TYPE_PHONE),
                    clientRecordId = "walkanywhere-steps-min-${s.epochSecond}",
                    clientRecordVersion = steps.toLong()
                )
            )
        }

        records.chunked(1000).forEach { hc.insertRecords(it) }
        (applicationContext as WalkAnywhereApplication).stepRepository.stepsSinceLastChecked()

        checkIfRouteCompleted()

        return Result.success()
    }

    suspend fun checkIfRouteCompleted() {
        val tripDao = AppDatabase.getInstance(applicationContext).tripDao()
        val routeWithSteps = tripDao.getSelectedRoute().first()
        routeWithSteps?.let { rws ->
            if (rws.stepsSinceReset >= rws.route.totalSteps && !rws.route.completionNotified) {
                postNotification()
                tripDao.setCompletionNotified(rws.route.routeId, true)
            }
        }
    }

    fun postNotification(){
        val intent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(applicationContext, "route_progress")
            .setSmallIcon(R.drawable.ic_directions_walk)
            .setContentTitle("Route Complete")
            .setContentText("You've completed you're selected route, tap here to set a new one")
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(applicationContext).notify(1001, notif)
        }
    }
}