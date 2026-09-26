package com.zachupstone.walkanywhere

import android.app.Application
import com.zachupstone.walkanywhere.health.StepRepository

class WalkAnywhereApplication: Application() {
    val stepRepository by lazy { StepRepository(this) }
}