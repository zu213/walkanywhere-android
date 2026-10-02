package com.zachupstone.walkanywhere

import android.app.Application
import com.zachupstone.walkanywhere.health.StepRepository
import com.zachupstone.walkanywhere.health.StepSyncer

class WalkAnywhereApplication: Application() {
    val stepRepository by lazy { StepRepository(this) }
    val stepSyncer by lazy { StepSyncer(this) }

}