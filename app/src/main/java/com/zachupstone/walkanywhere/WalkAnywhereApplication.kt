package com.zachupstone.walkanywhere

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.zachupstone.walkanywhere.data.health.StepRepository
import com.zachupstone.walkanywhere.data.health.StepSyncer

class WalkAnywhereApplication: Application() {
    val stepRepository by lazy { StepRepository(this) }
    val stepSyncer by lazy { StepSyncer(this) }

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            "route_progress",
            "Background updates",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        applicationContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

    }
}