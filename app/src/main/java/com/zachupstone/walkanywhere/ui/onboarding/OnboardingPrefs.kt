package com.zachupstone.walkanywhere.ui.onboarding

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class OnboardingPrefs(private val context: Context) {
    private val KEY = booleanPreferencesKey("onboarding_seen")

    val seen: Flow<Boolean> = context.dataStore.data.map { it[KEY] ?: false }

    suspend fun markSeen() {
        context.dataStore.edit { it[KEY] = true }
    }
}